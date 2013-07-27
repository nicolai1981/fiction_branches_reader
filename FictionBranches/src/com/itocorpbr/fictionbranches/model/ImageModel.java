package com.itocorpbr.fictionbranches.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.itocorpbr.fictionbranches.common.model.database.DatabaseManager;
import com.itocorpbr.fictionbranches.common.model.database.DatabaseTable;

public class ImageModel extends DatabaseTable {
    /**
     * Model/Table name.
     */
    public static final String MODEL_NAME = "fictionbranchesimage";

    /**
     * Columns names.
     */
    public static final String COLUMN_URL = "image_url";
    public static final String COLUMN_PATH = "image_path";
    public static final String COLUMN_DOWNLOADED = "image_downloaded";

    /**
     * Projection used for all queries.
     */
    private static final String[] PROJECTION = {
            _ID,
            COLUMN_URL,
            COLUMN_PATH,
            COLUMN_DOWNLOADED,
    };

    /**
     * Sort criteria for all queries.
     */
    private static final String ORDER_BY = COLUMN_URL;

    /**
     * SQL statement to create the table.
     */
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
            MODEL_NAME + " (" +
            _ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_URL        + " TEXT, " +
            COLUMN_PATH       + " TEXT, " +
            COLUMN_DOWNLOADED + " INTEGER);";

    /**
     * LatestChapterModel Constructor.
     * 
     * @param context the application context.
     * @param dbHelper the database manager to store the table.
     */
    public ImageModel(Context context, DatabaseManager dbHelper) {
        super(context, dbHelper);
    }

    public Cursor getChapterList() {
        return query(PROJECTION, null, null, null);
    }

    public ImageURL getImage(String url) {
        Cursor cursor = query(PROJECTION, COLUMN_URL + "='" + url + "'", null, ORDER_BY);
        if (cursor.moveToFirst()) {
            return toImage(cursor);
        }
        return null;
    }

    public boolean updateImage(ImageURL image) {
        lock();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, image.mURL);
            values.put(COLUMN_PATH, image.mPath);
            values.put(COLUMN_DOWNLOADED, image.mDownloaded);
            return update(values, COLUMN_URL + "='" + image.mURL + "'", null);
        } finally {
            unlock();
        }
    }

    public boolean deleteImage(String url) {
        lock();
        try {
            String selection = COLUMN_URL + " ='?'";
            String selectionArgs[] = new String[] {
                String.valueOf(url)
            };
            return delete(selection, selectionArgs);
        } finally {
            unlock();
        }
    }

    public boolean deleteAll() {
        lock();
        try {
            return delete(null, null);
        } finally {
            unlock();
        }
    }

    public boolean insertImage(ImageURL image) {
        lock();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, image.mURL);
            values.put(COLUMN_PATH, image.mPath);
            values.put(COLUMN_DOWNLOADED, image.mDownloaded);
            return replace(values, null, null);
        } finally {
            unlock();
        }
    }

    public ImageBatchOperation getBatchOperation() {
        return new ImageBatchOperation();
    }

    public class ImageBatchOperation extends BatchOperation {

        public void deleteChapter(final String page) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    ImageModel.this.deleteImage(page);
                }
            });
        }

        public void insertImage(final ImageURL image) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    ImageModel.this.insertImage(image);
                }
            });
        }

        public void updateImage(final ImageURL image) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    ImageModel.this.updateImage(image);
                }
            });
        }
    }

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    @Override
    public void onCreateModel() {
        execSQL(CREATE_TABLE);
    }

    public static ImageURL toImage(Cursor cursor) {
        ImageURL image = new ImageURL();
        image.mURL = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL));
        image.mPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PATH));
        image.mDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DOWNLOADED));
        return image;
    }
}
