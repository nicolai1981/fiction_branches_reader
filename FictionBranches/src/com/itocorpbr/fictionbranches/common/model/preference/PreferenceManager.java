package com.itocorpbr.fictionbranches.common.model.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.itocorpbr.fictionbranches.common.model.ModelManager;

public class PreferenceManager extends ModelManager {

    private SharedPreferences mPreferences;

    public PreferenceManager(Context context, String name, int version) {
        super(context, name, version);
    }

    public SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences =  getContext().getSharedPreferences(getName(), Context.MODE_PRIVATE);
            checkVersion();
        }
        return mPreferences;
    }

    public Editor getPreferencesEditor() {
        return getPreferences().edit();
    }

    @Override
    protected void onDeleteModel() {
        Editor editor = getPreferencesEditor();
        editor.clear();
        editor.commit();
    }
}
