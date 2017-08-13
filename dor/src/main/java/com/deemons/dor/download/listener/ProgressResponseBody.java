package com.deemons.dor.download.listener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/9 17:46
 * 包名       com.deemons.network.download
 * 描述
 */

public class ProgressResponseBody extends ResponseBody {
    private ResponseBody responseBody;
    private ProgressListener mListener;

    private BufferedSource bufferedSource;
    public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
        this.responseBody = responseBody;
        this.mListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long bytesReaded = 0;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                bytesReaded += bytesRead == -1 ? 0 : bytesRead;
                //实时发送当前已读取的字节和总字节
                if (mListener != null) {
                    mListener.progress(contentLength(), bytesReaded);
                }
                return bytesRead;
            }
        };
    }
}
