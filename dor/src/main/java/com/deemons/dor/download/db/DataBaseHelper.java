package com.deemons.dor.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.deemons.dor.download.constant.Flag;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.entity.DownloadRecord;
import com.deemons.dor.download.entity.Status;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.deemons.dor.download.constant.Flag.PAUSED;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_DATE;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_DOWNLOAD_FLAG;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_DOWNLOAD_SIZE;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_EXTRA1;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_EXTRA2;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_EXTRA3;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_EXTRA4;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_EXTRA5;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_ID;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_IS_CHUNKED;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_MISSION_ID;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_SAVE_NAME;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_SAVE_PATH;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_TOTAL_SIZE;
import static com.deemons.dor.download.db.Db.RecordTable.COLUMN_URL;
import static com.deemons.dor.download.db.Db.RecordTable.TABLE_NAME;
import static com.deemons.dor.download.db.Db.RecordTable.insert;
import static com.deemons.dor.download.db.Db.RecordTable.read;
import static com.deemons.dor.download.db.Db.RecordTable.update;


/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/14
 * Time: 10:02
 * FIXME
 */
public class DataBaseHelper {
    private volatile static DataBaseHelper singleton;
    private final Object databaseLock = new Object();
    private DbOpenHelper mDbOpenHelper;
    private volatile SQLiteDatabase readableDatabase;
    private volatile SQLiteDatabase writableDatabase;

    private DataBaseHelper(Context context) {
        mDbOpenHelper = new DbOpenHelper(context);
    }

    public static DataBaseHelper getSingleton(Context context) {
        if (singleton == null) {
            synchronized (DataBaseHelper.class) {
                if (singleton == null) {
                    singleton = new DataBaseHelper(context);
                }
            }
        }
        return singleton;
    }

    public static DataBaseHelper getSingleton() {
        if (singleton == null) {
            throw new NullPointerException("DataBaseHelper is not init");
        }
        return singleton;
    }

    /**
     * Judge the url's mBean exists.
     *
     * @param url url
     * @return true if not exists
     */
    public boolean recordNotExists(String url) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE_NAME, new String[]{COLUMN_ID},
                    COLUMN_URL + "=?", new String[]{url}, null, null, null);
            cursor.moveToFirst();
            return cursor.getCount() == 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public long insertRecord(DownloadBean downloadBean, int flag) {
        return getWritableDatabase().insert(TABLE_NAME, null, insert(downloadBean, flag, null));
    }

    public long insertRecord(DownloadBean downloadBean, int flag, String missionId) {
        return getWritableDatabase().insert(TABLE_NAME, null, insert(downloadBean, flag, missionId));
    }

    public long updateStatus(String url, Status status) {
        return getWritableDatabase().update(TABLE_NAME, update(status),
                COLUMN_URL + "=?", new String[]{url});
    }

    public long updateRecord(String url, int flag) {
        return getWritableDatabase().update(TABLE_NAME, update(flag),
                COLUMN_URL + "=?", new String[]{url});
    }

    public long updateRecord(String url, int flag, String missionId) {
        return getWritableDatabase().update(TABLE_NAME, update(flag, missionId),
                COLUMN_URL + "=?", new String[]{url});
    }

    public long updateRecord(String url, String saveName, String savePath, int flag) {
        return getWritableDatabase().update(TABLE_NAME, update(saveName, savePath, flag),
                COLUMN_URL + "=?", new String[]{url});
    }

    public int deleteRecord(String url) {
        return getWritableDatabase().delete(TABLE_NAME, COLUMN_URL + "=?", new String[]{url});
    }

    public long repairErrorFlag() {
        return getWritableDatabase().update(TABLE_NAME, update(PAUSED),
                COLUMN_DOWNLOAD_FLAG + "=? or " + COLUMN_DOWNLOAD_FLAG + "=?",
                new String[]{Flag.WAITING + "", Flag.STARTED + ""});
    }

    /**
     * Read single Record.
     *
     * @param url url
     * @return Record
     */
    @Nullable
    public DownloadRecord readSingleRecord(String url) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE_NAME,
                    new String[]{COLUMN_ID, COLUMN_URL, COLUMN_SAVE_NAME, COLUMN_SAVE_PATH,
                            COLUMN_DOWNLOAD_SIZE, COLUMN_TOTAL_SIZE, COLUMN_IS_CHUNKED,
                            COLUMN_EXTRA1, COLUMN_EXTRA2, COLUMN_EXTRA3, COLUMN_EXTRA4,
                            COLUMN_EXTRA5, COLUMN_DOWNLOAD_FLAG, COLUMN_DATE, COLUMN_MISSION_ID},
                    COLUMN_URL + "=?", new String[]{url}, null, null, null);
            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                return null;
            } else {
                return read(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Read missionId all records.
     *
     * @param missionId missionId
     * @return Records
     */
    public List<DownloadRecord> readMissionsRecord(String missionId) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE_NAME,
                    new String[]{COLUMN_ID, COLUMN_URL, COLUMN_SAVE_NAME, COLUMN_SAVE_PATH,
                            COLUMN_DOWNLOAD_SIZE, COLUMN_TOTAL_SIZE, COLUMN_IS_CHUNKED,
                            COLUMN_EXTRA1, COLUMN_EXTRA2, COLUMN_EXTRA3, COLUMN_EXTRA4,
                            COLUMN_EXTRA5, COLUMN_DOWNLOAD_FLAG, COLUMN_DATE, COLUMN_MISSION_ID},
                    COLUMN_MISSION_ID + "=?", new String[]{missionId}, null, null, null);
            List<DownloadRecord> result = new ArrayList<>();
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    result.add(read(cursor));
                } while (cursor.moveToNext());
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Read the url's download status.
     *
     * @param url url
     * @return download status
     */
    public Status readStatus(String url) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(
                    TABLE_NAME,
                    new String[]{COLUMN_DOWNLOAD_SIZE, COLUMN_TOTAL_SIZE, COLUMN_IS_CHUNKED},
                    COLUMN_URL + "=?", new String[]{url}, null, null, null);
            cursor.moveToFirst();
            if (cursor.getCount() == 0) {
                return new Status();
            } else {
                return Db.RecordTable.readStatus(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Read all records from database.
     *
     * @return All records.
     */
    public Observable<List<DownloadRecord>> readAllRecords() {
        return Observable
                .create(new ObservableOnSubscribe<List<DownloadRecord>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<DownloadRecord>> emitter)
                            throws Exception {
                        Cursor cursor = null;
                        try {
                            cursor = getReadableDatabase().query(TABLE_NAME,
                                    new String[]{COLUMN_ID, COLUMN_URL, COLUMN_SAVE_NAME, COLUMN_SAVE_PATH,
                                            COLUMN_DOWNLOAD_SIZE, COLUMN_TOTAL_SIZE, COLUMN_IS_CHUNKED,
                                            COLUMN_EXTRA1, COLUMN_EXTRA2, COLUMN_EXTRA3, COLUMN_EXTRA4,
                                            COLUMN_EXTRA5, COLUMN_DOWNLOAD_FLAG, COLUMN_DATE, COLUMN_MISSION_ID},
                                    null, null, null, null, null);
                            List<DownloadRecord> result = new ArrayList<>();
                            cursor.moveToFirst();
                            if (cursor.getCount() > 0) {
                                do {
                                    result.add(read(cursor));
                                } while (cursor.moveToNext());
                            }
                            emitter.onNext(result);
                            emitter.onComplete();
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Read the url's mBean.
     * <p>
     * If mBean not exists, return an empty mBean.
     *
     * @param url url
     * @return mBean
     */
    public Observable<DownloadRecord> readRecord(final String url) {
        return Observable
                .create(new ObservableOnSubscribe<DownloadRecord>() {
                    @Override
                    public void subscribe(ObservableEmitter<DownloadRecord> emitter) throws Exception {
                        Cursor cursor = null;
                        try {
                            cursor = getReadableDatabase().query(TABLE_NAME,
                                    new String[]{COLUMN_ID, COLUMN_URL, COLUMN_SAVE_NAME, COLUMN_SAVE_PATH,
                                            COLUMN_DOWNLOAD_SIZE, COLUMN_TOTAL_SIZE, COLUMN_IS_CHUNKED,
                                            COLUMN_EXTRA1, COLUMN_EXTRA2, COLUMN_EXTRA3, COLUMN_EXTRA4,
                                            COLUMN_EXTRA5, COLUMN_DOWNLOAD_FLAG, COLUMN_DATE, COLUMN_MISSION_ID},
                                    COLUMN_URL + "=?", new String[]{url}, null, null, null);
                            cursor.moveToFirst();
                            if (cursor.getCount() == 0) {
                                emitter.onNext(new DownloadRecord());
                            } else {
                                emitter.onNext(read(cursor));
                            }
                            emitter.onComplete();
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void closeDataBase() {
        synchronized (databaseLock) {
            readableDatabase = null;
            writableDatabase = null;
            mDbOpenHelper.close();
        }
    }

    private SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = writableDatabase;
        if (db == null) {
            synchronized (databaseLock) {
                db = writableDatabase;
                if (db == null) {
                    db = writableDatabase = mDbOpenHelper.getWritableDatabase();
                }
            }
        }
        return db;
    }

    private SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = readableDatabase;
        if (db == null) {
            synchronized (databaseLock) {
                db = readableDatabase;
                if (db == null) {
                    db = readableDatabase = mDbOpenHelper.getReadableDatabase();
                }
            }
        }
        return db;
    }
}