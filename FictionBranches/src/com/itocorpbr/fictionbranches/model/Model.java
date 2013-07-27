package com.itocorpbr.fictionbranches.model;

import android.content.Context;

import com.itocorpbr.fictionbranches.common.model.database.DatabaseManager;
import com.itocorpbr.fictionbranches.common.model.preference.PreferenceManager;

public class Model {
    /**
     * Stores the Model version. It shall be incremented for each Model change
     * to allow upgrade/downgrade.
     */
    private static final int MODEL_VERSION = 1;

    /**
     * Preference file name.
     */
    private static final String PREFERENCES_NAME = "fiction_branches";

    /**
     * Database file name.
     */
    private static final String DATABASE_NAME = "fiction_branches.db";

    /**
     * Preferences manager instance.
     */
    private final PreferenceManager mPreferenceManager;

    /**
     * Database manager instance.
     */
    private final DatabaseManager mDbManager;

    public Model(Context context) {
        mDbManager = new DatabaseManager(context, DATABASE_NAME, MODEL_VERSION);
        mDbManager.addModel(new ChapterModel(context, mDbManager));
        mDbManager.addModel(new LatestChapterModel(context, mDbManager));
        mDbManager.addModel(new ImageModel(context, mDbManager));

        mPreferenceManager = new PreferenceManager(context, PREFERENCES_NAME, MODEL_VERSION);
        mPreferenceManager.addModel(new AccountModel(context, mPreferenceManager));
    }

    public AccountModel getAccountModel() {
        return (AccountModel) mPreferenceManager.getModel(AccountModel.MODEL_NAME);
    }

    public ChapterModel getChapterModel() {
        return (ChapterModel) mDbManager.getModel(ChapterModel.MODEL_NAME);
    }

    public LatestChapterModel getLatestChapterModel() {
        return (LatestChapterModel) mDbManager.getModel(LatestChapterModel.MODEL_NAME);
    }

    public ImageModel getImageModel() {
        return (ImageModel) mDbManager.getModel(ImageModel.MODEL_NAME);
    }
}
