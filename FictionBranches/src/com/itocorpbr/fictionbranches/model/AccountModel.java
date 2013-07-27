package com.itocorpbr.fictionbranches.model;

import android.content.Context;
import android.content.SharedPreferences.Editor;

import com.itocorpbr.fictionbranches.common.model.preference.PreferenceManager;
import com.itocorpbr.fictionbranches.common.model.preference.PreferenceModel;

public class AccountModel extends PreferenceModel {

    public static final String MODEL_NAME = "account";
    private static final String LAST_CHAPTER = "last_chapter";

    public AccountModel(Context context, PreferenceManager manager) {
        super(context, manager);
    }

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    public void setLastChapter(String page) {
        Editor editor = getPreferencesEditor();
        editor.putString(getKey(LAST_CHAPTER), page);
        editor.commit();
    }

    public String getLastChapter() {
        return getPreferences().getString(getKey(LAST_CHAPTER), "root");
    }
}
