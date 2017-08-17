package com.deemons.dor.download.file;

import android.net.TrafficStats;

import com.deemons.dor.download.entity.DownloadRange;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.utils.ResponesUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;

import io.reactivex.FlowableEmitter;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.deemons.dor.download.constant.Constant.CHUNKED_DOWNLOAD_HINT;
import static com.deemons.dor.utils.ResponesUtils.GMTToLong;
import static com.deemons.dor.utils.ResponesUtils.closeQuietly;
import static com.deemons.dor.utils.ResponesUtils.log;
import static com.deemons.dor.utils.ResponesUtils.longToGMT;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;


/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/17
 * Time: 10:35
 * <p>
 * File Helper
 */
public class FileHelper {
    private static final int EACH_RECORD_SIZE = 16; //long + long = 8 + 8
    private static final String ACCESS = "rw";
    private int RECORD_FILE_TOTAL_SIZE;
    //|*********************|
    //|*****Record  File****|
    //|*********************|
    //|  0L      |     7L   | 0
    //|  8L      |     15L  | 1
    //|  16L     |     31L  | 2
    //|  ...     |     ...  | maxThreads-1
    //|*********************|
    private int maxThreads;
    private int uid;

    public FileHelper(int maxThreads, int uid) {
        this.maxThreads = maxThreads;
        this.uid = uid;
        RECORD_FILE_TOTAL_SIZE = EACH_RECORD_SIZE * maxThreads;
    }

    public void prepareDownload(File lastModifyFile, File saveFile, long fileLength, String lastModify)
            throws IOException, ParseException {

        writeLastModify(lastModifyFile, lastModify);
        prepareFile(saveFile, fileLength);
    }

    public void saveFile(FlowableEmitter<Status> emitter, File saveFile,
                         Response<ResponseBody> resp) {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            try {
                int readLen;
                int downloadSize = 0;
                byte[] buffer = new byte[8192];

                Status status = new Status();
                inputStream = resp.body().byteStream();
                outputStream = new FileOutputStream(saveFile);

                long contentLength = resp.body().contentLength();

                boolean isChunked = ResponesUtils.isChunked(resp);
                if (isChunked || contentLength == -1) {
                    status.isChunked = true;
                }

                status.totalSize = contentLength;

                while ((readLen = inputStream.read(buffer)) != -1 && !emitter.isCancelled()) {
                    outputStream.write(buffer, 0, readLen);
                    downloadSize += readLen;
                    status.downloadSize = downloadSize;
                    emitter.onNext(status);
                }

                outputStream.flush(); // This is important!!!
                emitter.onComplete();
            } finally {
                closeQuietly(inputStream);
                closeQuietly(outputStream);
                closeQuietly(resp.body());
            }
        } catch (IOException e) {
            emitter.onError(e);
        }
    }

    public void prepareDownload(File lastModifyFile, File tempFile, File saveFile,
                                long fileLength, String lastModify)
            throws IOException, ParseException {

        writeLastModify(lastModifyFile, lastModify);
        prepareFile(tempFile, saveFile, fileLength);
    }

    public void saveFile(FlowableEmitter<Status> emitter, int i, File tempFile,
                         File saveFile, ResponseBody response) {
        RandomAccessFile record = null;
        FileChannel recordChannel = null;
        RandomAccessFile save = null;
        FileChannel saveChannel = null;
        InputStream inStream = null;
        log("Thread: " + Thread.currentThread().getName() + "; saveFile start, content length: " + response.contentLength());
        try {
            try {
                int readLen;
                byte[] buffer = new byte[2048];

                Status status = new Status();
                status.startTimeStamp = lastTimeStamp;
                record = new RandomAccessFile(tempFile, ACCESS);
                recordChannel = record.getChannel();
                MappedByteBuffer recordBuffer = recordChannel
                        .map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);

                int startIndex = i * EACH_RECORD_SIZE;

                long start = recordBuffer.getLong(startIndex);
                long oldStart = start;
                //                long end = recordBuffer.getLong(startIndex + 8);

                long totalSize = recordBuffer.getLong(RECORD_FILE_TOTAL_SIZE - 8) + 1;
                status.totalSize = totalSize;

                save = new RandomAccessFile(saveFile, ACCESS);
                saveChannel = save.getChannel();

                inStream = response.byteStream();

                while ((readLen = inStream.read(buffer)) != -1 && !emitter.isCancelled()) {
                    MappedByteBuffer saveBuffer = saveChannel.map(READ_WRITE, start, readLen);
                    start += readLen;
                    if (start - oldStart > 100000L) {
                        log("Thread: " + Thread.currentThread().getName() + "; saveLenRead: " + start);
                        oldStart = start;
                    }
                    saveBuffer.put(buffer, 0, readLen);
                    recordBuffer.putLong(startIndex, start);
                    status.downloadSize = totalSize - getResidue(recordBuffer);

                    emitter.onNext(status);
                }
                emitter.onComplete();
            } finally {
                closeQuietly(record);
                closeQuietly(recordChannel);
                closeQuietly(save);
                closeQuietly(saveChannel);
                closeQuietly(inStream);
                closeQuietly(response);
            }
        } catch (IOException e) {
            emitter.onError(e);
        }
    }


    long lastTotalRxBytes = 0L;
    long lastTimeStamp = 0L;
    long nowTotalRxBytes;
    long nowTimeStamp;
    long speed;

    public Status getDownloadSpeed(Status status) {
        if (lastTimeStamp == 0) {
            lastTimeStamp = status.startTimeStamp;
        }

        nowTotalRxBytes = TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getTotalRxBytes();
        nowTimeStamp = System.currentTimeMillis();
        speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换


        status.speed = ResponesUtils.formatSize(speed);

        lastTotalRxBytes = nowTotalRxBytes;
        lastTimeStamp = nowTimeStamp;

        return status;

    }

    public boolean fileNotComplete(File tempFile) throws IOException {

        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(tempFile, ACCESS);
            channel = record.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);

            long startByte;
            long endByte;
            for (int i = 0; i < maxThreads; i++) {
                startByte = buffer.getLong();
                endByte = buffer.getLong();
                if (startByte <= endByte) {
                    return true;
                }
            }
            return false;
        } finally {
            closeQuietly(channel);
            closeQuietly(record);
        }
    }

    public boolean tempFileDamaged(File tempFile, long fileLength) throws IOException {

        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(tempFile, ACCESS);
            channel = record.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);
            long recordTotalSize = buffer.getLong(RECORD_FILE_TOTAL_SIZE - 8) + 1;
            return recordTotalSize != fileLength;
        } finally {
            closeQuietly(channel);
            closeQuietly(record);
        }
    }

    public DownloadRange readDownloadRange(File tempFile, int i) throws IOException {

        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(tempFile, ACCESS);
            channel = record.getChannel();
            MappedByteBuffer buffer = channel
                    .map(READ_WRITE, i * EACH_RECORD_SIZE, (i + 1) * EACH_RECORD_SIZE);
            long startByte = buffer.getLong();
            long endByte = buffer.getLong();
            return new DownloadRange(startByte, endByte);
        } finally {
            closeQuietly(channel);
            closeQuietly(record);
        }
    }

    public String readLastModify(File lastModifyFile) throws IOException {

        RandomAccessFile record = null;
        try {
            record = new RandomAccessFile(lastModifyFile, ACCESS);
            record.seek(0);
            return longToGMT(record.readLong());
        } finally {
            closeQuietly(record);
        }
    }

    private void prepareFile(File tempFile, File saveFile, long fileLength)
            throws IOException {
        RandomAccessFile rFile = null;
        RandomAccessFile rRecord = null;
        FileChannel channel = null;
        try {
            rFile = new RandomAccessFile(saveFile, ACCESS);
            rFile.setLength(fileLength);//设置下载文件的长度

            rRecord = new RandomAccessFile(tempFile, ACCESS);
            rRecord.setLength(RECORD_FILE_TOTAL_SIZE); //设置指针记录文件的大小

            channel = rRecord.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);

            long start;
            long end;
            int eachSize = (int) (fileLength / maxThreads);

            for (int i = 0; i < maxThreads; i++) {
                if (i == maxThreads - 1) {
                    start = i * eachSize;
                    end = fileLength - 1;
                } else {
                    start = i * eachSize;
                    end = (i + 1) * eachSize - 1;
                }
                buffer.putLong(start);
                buffer.putLong(end);
            }
        } finally {
            closeQuietly(channel);
            closeQuietly(rRecord);
            closeQuietly(rFile);
        }
    }

    private void prepareFile(File saveFile, long fileLength) throws IOException {

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(saveFile, ACCESS);
            if (fileLength != -1) {
                file.setLength(fileLength);//设置下载文件的长度
            } else {
                log(CHUNKED_DOWNLOAD_HINT);
                //Chunked 下载, 无需设置文件大小.
            }
        } finally {
            closeQuietly(file);
        }
    }

    private void writeLastModify(File file, String lastModify)
            throws IOException, ParseException {

        RandomAccessFile record = null;
        try {
            record = new RandomAccessFile(file, ACCESS);
            record.setLength(8);
            record.seek(0);
            record.writeLong(GMTToLong(lastModify));
        } finally {
            closeQuietly(record);
        }
    }

    /**
     * 还剩多少字节没有下载
     *
     * @param recordBuffer buffer
     * @return 剩余的字节
     */
    private long getResidue(MappedByteBuffer recordBuffer) {
        long residue = 0;
        for (int j = 0; j < maxThreads; j++) {
            long startTemp = recordBuffer.getLong(j * EACH_RECORD_SIZE);
            long endTemp = recordBuffer.getLong(j * EACH_RECORD_SIZE + 8);
            long temp = endTemp - startTemp + 1;
            residue += temp;
        }
        return residue;
    }


}
