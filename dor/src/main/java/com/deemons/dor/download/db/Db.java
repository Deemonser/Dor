package com.deemons.dor.download.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.entity.DownloadRecord;
import com.deemons.dor.download.entity.Status;

import java.util.Date;

import static com.deemons.dor.utils.ResponesUtils.empty;


/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/14
 * Time: 10:02
 * FIXME
 */
class Db {
    private Db() {
    }

    static final class RecordTable {
        static final String TABLE_NAME = "download_record";

        static final String COLUMN_ID = "id";
        static final String COLUMN_URL = "url";
        static final String COLUMN_SAVE_NAME = "save_name";
        static final String COLUMN_SAVE_PATH = "save_path";
        static final String COLUMN_DOWNLOAD_SIZE = "download_size";
        static final String COLUMN_TOTAL_SIZE = "total_size";
        static final String COLUMN_IS_CHUNKED = "is_chunked";
        static final String COLUMN_DOWNLOAD_FLAG = "download_flag";
        static final String COLUMN_EXTRA1 = "extra1";
        static final String COLUMN_EXTRA2 = "extra2";
        static final String COLUMN_EXTRA3 = "extra3";
        static final String COLUMN_EXTRA4 = "extra4";
        static final String COLUMN_EXTRA5 = "extra5";
        static final String COLUMN_DATE = "date";
        static final String COLUMN_MISSION_ID = "mission_id";

        static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_URL + " TEXT NOT NULL," +
                        COLUMN_SAVE_NAME + " TEXT," +
                        COLUMN_SAVE_PATH + " TEXT," +
                        COLUMN_TOTAL_SIZE + " INTEGER," +
                        COLUMN_DOWNLOAD_SIZE + " INTEGER," +
                        COLUMN_IS_CHUNKED + " INTEGER," +
                        COLUMN_DOWNLOAD_FLAG + " INTEGER," +
                        COLUMN_EXTRA1 + " TEXT," +
                        COLUMN_EXTRA2 + " TEXT," +
                        COLUMN_EXTRA3 + " TEXT," +
                        COLUMN_EXTRA4 + " TEXT," +
                        COLUMN_EXTRA5 + " TEXT," +
                        COLUMN_DATE + " INTEGER NOT NULL, " +
                        COLUMN_MISSION_ID + " TEXT " +
                        " )";

        static final String ALTER_TABLE_ADD_EXTRA1 = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_EXTRA1 + " TEXT";
        static final String ALTER_TABLE_ADD_EXTRA2 = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_EXTRA2 + " TEXT";
        static final String ALTER_TABLE_ADD_EXTRA3 = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_EXTRA3 + " TEXT";
        static final String ALTER_TABLE_ADD_EXTRA4 = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_EXTRA4 + " TEXT";
        static final String ALTER_TABLE_ADD_EXTRA5 = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_EXTRA5 + " TEXT";
        static final String ALTER_TABLE_ADD_MISSION_ID = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMN_MISSION_ID + " TEXT";

        static ContentValues insert(DownloadBean bean, int flag, String missionId) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, bean.url);
            values.put(COLUMN_SAVE_NAME, bean.saveName);
            values.put(COLUMN_SAVE_PATH, bean.savePath);
            values.put(COLUMN_DOWNLOAD_FLAG, flag);
            values.put(COLUMN_EXTRA1, bean.extra1);
            values.put(COLUMN_EXTRA2, bean.extra2);
            values.put(COLUMN_EXTRA3, bean.extra3);
            values.put(COLUMN_EXTRA4, bean.extra4);
            values.put(COLUMN_EXTRA5, bean.extra5);
            values.put(COLUMN_DATE, new Date().getTime());
            if (empty(missionId)) {
                values.put(COLUMN_MISSION_ID, missionId);
            }
            return values;
        }

        static ContentValues update(Status status) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_CHUNKED, status.isChunked);
            values.put(COLUMN_DOWNLOAD_SIZE, status.downloadSize);
            values.put(COLUMN_TOTAL_SIZE, status.totalSize);
            return values;
        }

        static ContentValues update(int flag) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DOWNLOAD_FLAG, flag);
            return values;
        }

        static ContentValues update(int flag, String missionId) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DOWNLOAD_FLAG, flag);
            if (empty(missionId)) {
                values.put(COLUMN_MISSION_ID, missionId);
            }
            return values;
        }

        static ContentValues update(String saveName, String savePath, int flag) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SAVE_NAME, saveName);
            values.put(COLUMN_SAVE_PATH, savePath);
            values.put(COLUMN_DOWNLOAD_FLAG, flag);
            return values;
        }

        static Status readStatus(Cursor cursor) {
            boolean isSection = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_CHUNKED)) > 0;
            long downloadSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DOWNLOAD_SIZE));
            long totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SIZE));
            return new Status(isSection, downloadSize, totalSize);
        }

        static DownloadRecord read(Cursor cursor) {
            DownloadRecord record = new DownloadRecord();
            record.id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            record.url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL));
            record.saveName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVE_NAME));
            record.savePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVE_PATH));

            boolean isSection = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_CHUNKED)) > 0;
            long downloadSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DOWNLOAD_SIZE));
            long totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SIZE));
            record.status = new Status(isSection, downloadSize, totalSize);

            record.extra1 =cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRA1));
            record.extra2 =cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRA2));
            record.extra3 =cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRA3));
            record.extra4 =cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRA4));
            record.extra5 =cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXTRA5));

            record.flag = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DOWNLOAD_FLAG));
            record.date =cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE));
            record.missionId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MISSION_ID));
            return record;
        }
    }
}
