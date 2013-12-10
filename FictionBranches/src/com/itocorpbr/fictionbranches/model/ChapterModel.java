package com.itocorpbr.fictionbranches.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.itocorpbr.fictionbranches.common.model.database.DatabaseManager;
import com.itocorpbr.fictionbranches.common.model.database.DatabaseTable;

public class ChapterModel extends DatabaseTable {
    /**
     * Model/Table name.
     */
    public static final String MODEL_NAME = "fictionbrancheschapter";

    /**
     * Columns names.
     */
    public static final String COLUMN_PARENT = "chapter_parent";
    public static final String COLUMN_PAGE = "chapter_page";
    public static final String COLUMN_TITLE = "chapter_title";
    public static final String COLUMN_CONTENT = "chapter_content";
    public static final String COLUMN_AUTHOR = "chapter_author";
    public static final String COLUMN_DATE = "chapter_date";
    public static final String COLUMN_READ = "chapter_read";
    public static final String COLUMN_FAV = "chapter_fav";

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
    private static final String ORDER_BY = _ID;

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
     * ChapterModel Constructor.
     * 
     * @param context the application context.
     * @param dbHelper the database manager to store the table.
     */
    public ChapterModel(Context context, DatabaseManager dbHelper) {
        super(context, dbHelper);
    }

    public Cursor getChildList(String parentPage) {
        return query(PROJECTION, COLUMN_PARENT + "='" + parentPage + "'", null, ORDER_BY);
    }

    public Cursor getUnreadList() {
        return query(PROJECTION, COLUMN_READ + "=0", null, null);
    }

    public Cursor getChapterCursor(String page) {
        return query(PROJECTION, COLUMN_PAGE + "='" + page + "' AND " + COLUMN_TITLE + "!='BACK'", null, ORDER_BY);
    }

    public Chapter getChapter(String page) {
        Chapter chapter = null;
        Cursor cursor = query(PROJECTION, COLUMN_PAGE + "='" + page + "' AND " + COLUMN_TITLE + "!='BACK'", null, ORDER_BY);
        if (cursor.moveToFirst()) {
            chapter = toChapter(cursor);
        }
        cursor.close();
        return chapter;
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

    public boolean updateBackChapter(Chapter chapter) {
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
            return update(values, COLUMN_PAGE + "='" + chapter.mPage + "' AND " + COLUMN_TITLE + "='BACK'", null);
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

    public ChapterBatchOperation getBatchOperation() {
        return new ChapterBatchOperation();
    }

    public class ChapterBatchOperation extends BatchOperation {

        public void deleteChapter(final String page) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    ChapterModel.this.deleteChapter(page);
                }
            });
        }

        public void insertUpdateChapter(final Chapter chapter) {
            addOperation(new Runnable() {
                @Override
                public void run() {
                    Chapter oldChapter = ChapterModel.this.getChapter(chapter.mPage);
                    if (oldChapter == null) {
                        ChapterModel.this.insertChapter(chapter);
                    } else if (oldChapter.mRead == 0) {
                        ChapterModel.this.updateChapter(chapter);
                    }
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
