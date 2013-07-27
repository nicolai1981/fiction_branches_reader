package com.itocorpbr.fictionbranches.common.model.preference;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.itocorpbr.fictionbranches.common.model.Model;

public abstract class PreferenceModel extends Model {

    private final PreferenceManager mManager;

    public PreferenceModel(Context context, PreferenceManager manager) {
        super(manager);
        mManager = manager;
    }

    protected SharedPreferences getPreferences() {
        return mManager.getPreferences();
    }

    protected Editor getPreferencesEditor() {
        return mManager.getPreferencesEditor();
    }

    protected String getKey(String name) {
        return getModelName() + "_" + name;
    }

    @Override
    public void onDeleteModel() {
        Map<String, ?> map = getPreferences().getAll();
        Editor editor = getPreferencesEditor();
        for (String key : map.keySet()) {
            if (key.startsWith(getModelName())) {
                editor.remove(key);
            }
        }
        editor.commit();
    }

    @Override
    public void onCreateModel() {
    }
}
