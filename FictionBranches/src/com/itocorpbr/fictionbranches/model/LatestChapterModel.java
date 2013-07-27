package com.itocorpbr.fictionbranches.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.itocorpbr.fictionbranches.common.model.database.DatabaseManager;
import com.itocorpbr.fictionbranches.common.model.database.DatabaseTable;

public class LatestChapterModel extends DatabaseTable {
    /**
     * Model/Table name.
     */
    public static final String MODEL_NAME = "fictionbrancheslatestchapter";

    /**
     * Columns names.
     */
    public static final String COLUMN_PARENT = "latestchapter_parent";
    public static final String COLUMN_PAGE = "latestchapter_page";
    public static final String COLUMN_TITLE = "latestchapter_title";
    public static final String COLUMN_CONTENT = "latestchapter_content";
    public static final String COLUMN_AUTHOR = "latestchapter_author";
    public static final String COLUMN_DATE = "latestchapter_date";
    public static final String COLUMN_READ = "latestchapter_read";
    public static final String COLUMN_FAV = "latestchapter_fav";

    /**
     * Projection used for all queries.
     */
    private static final String[] PROJECTION = {
            _ID,
            COLUMN_PARENT,
            COLUMN_PAGE,
            COLUMN_TITLE,
            COLUMN_CONTENT,
            COLUMN_AUTHOR,
            COLUMN_DATE,
            COLUMN_READ,
            COLUMN_FAV
    };

    /**
     * Sort criteria for all queries.
     */
    private static final String ORDER_BY = COLUMN_DATE;

    /**
     * SQL statement to create the table.
     */
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
            MODEL_NAME + " (" +
            _ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PARENT   + " TEXT, " +
            COLUMN_PAGE     + " TEXT, " +
            COLUMN_TITLE    + " TEXT, " +
            COLUMN_CONTENT  + " TEXT, " +
            COLUMN_AUTHOR   + " TEXT, " +
            COLUMN_DATE     + " INTEGER, " +
            COLUMN_READ     + " INTEGER," +
            COLUMN_FAV      + " INTEGER);";

    /**
     * LatestChapterModel Constructor.
     * 
     * @param context the application context.
     * @param dbHelper the database manager to store the table.
     */
    public LatestChapterModel(Context context, DatabaseManager dbHelper) {
        super(context, dbHelper);
    }

    public Cursor getChapterList() {
        return query(PROJECTION, null, null, null);
    }

    public Chapter getChapter(String page) {
        Cursor cursor = query(PROJECTION, COLUMN_PAGE + "='" + page + "'", null, ORDER_BY);
        if (cursor.moveToFirst()) {
            return toChapter(cursor);
        }
        return null;
    }

    public boolean updateChapter(Chapter chapter) {
        lock();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PARENT, chapter.mParent);
            values.put(COLUMN_PAGE, chapter.mPage);
            values.put(COLUMN_TITLE, chapter.mTitle);
            values.put(COLUMN_CONTENT, chapter.mContent);
            values.put(COLUMN_AUTHOR, chapter.mAuthor);
            values.put(COLUMN_DATE, chapter.mDate);
            values.put(COLUMN_READ, chapter.mRead);
            values.put(COLUMN_FAV, chapter.mFav);
            return update(values, COLUMN_PAGE + "='" + chapter.mPage + "' AND " + COLUMN_TITLE + "!='BACK'", null);
        } finally {
            unlock();
        }
    }

    public boolean deleteChapter(String page) {
        lock();
        try {
            String selection = COLUMN_PAGE + " ='?'";
            String selectionArgs[] = new String[] {
                String.valueOf(page)
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

    public boolean insertChapter(Chapter chapter) {
        lock();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PARENT, chapter.mParent);
            values.put(COLUMN_PAGE, chapter.mPage);
            values.put(COLUMN_TITLE, chapter.mTitle);
            values.put(COLUMN_CONTENT, chapter.mContent);
            values.put(COLUMN_AUTHOR, chapter.mAuthor);
            values.put(COLUMN_DATE, chapter.mDate);
            values.put(COLUMN_READ, chapter.mRead);
            values.put(COLUMN_FAV, chapter.mFav);
            return replace(values, null, null);
        } finally {
            unlock();
        }
    }

    public LatestChapterBatchOperation getBatchOperation() {
        return new LatestChapterBatchOperation();
    }

    public class LatestChapterBatchOperation extends BatchOperation {

        public void deleteChapter(final String page) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    LatestChapterModel.this.deleteChapter(page);
                }
            });
        }

        public void insertChapter(final Chapter chapter) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    LatestChapterModel.this.insertChapter(chapter);
                }
            });
        }

        public void updateChapter(final Chapter chapter) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    LatestChapterModel.this.updateChapter(chapter);
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

    public static Chapter toChapter(Cursor cursor) {
        Chapter chapter = new Chapter();
        chapter.mParent = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARENT));
        chapter.mPage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAGE));
        chapter.mTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        chapter.mContent = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
        chapter.mAuthor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR));
        chapter.mDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE));
        chapter.mRead = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_READ));
        chapter.mFav = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAV));
        return chapter;
    }
}
