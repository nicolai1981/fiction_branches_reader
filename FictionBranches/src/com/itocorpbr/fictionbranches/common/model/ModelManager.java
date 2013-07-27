package com.itocorpbr.fictionbranches.common.model;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class ModelManager {

    private static final String VERSION_KEY = "_version";
    private static final String PREFERENCE_NAME = "model_manager";

    private final ArrayList<Model> mModelList;
    private final NotificationLock mNotificationLock;
    private final Context          mContext;
    private final String           mName;
    private final int              mVersion;

    public ModelManager(Context context, String name, int version) {
        mContext = context;
        mName    = name;
        mVersion = version;
        mModelList = new ArrayList<Model>();
        mNotificationLock = new NotificationLock(mContext);
    }

    public void addModel(Model model) {
        mModelList.add(model);
        model.onAttachModel();
    }

    public Model getModel(String name) {
        for (Model model: mModelList) {
            if (TextUtils.equals(name, model.getModelName())) {
                return model;
            }
        }
        return null;
    }

    public Context getContext() {
        return mContext;
    }

    public String getName() {
        return mName;
    }

    public NotificationLock getNotificationLock() {
        return mNotificationLock;
    }

    protected void onDeleteModel() {
        for (Model model: mModelList) {
            model.onDeleteModel();
        }
    }

    protected void onCreateModel() {
        for (Model model: mModelList) {
            model.onCreateModel();
        }
    }

    protected void onUpgradeModel(int oldVersion, int newVersion) {
        for (Model model: mModelList) {
            model.onUpgradeModel(oldVersion, newVersion);
        }
    }

    protected void downgrade(int oldVersion, int newVersion) {
        for (Model model: mModelList) {
            model.onDowngradeModel(oldVersion, newVersion);
        }
    }

    protected void checkVersion() {
        SharedPreferences preferences = getContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        int oldVersion = preferences.getInt(mName + VERSION_KEY, 0);
        if (oldVersion == 0) {
            onDeleteModel();
            onCreateModel();
        } else if (oldVersion < mVersion) {
            onUpgradeModel(oldVersion, mVersion);
        } else if (oldVersion > mVersion) {
            downgrade(oldVersion, mVersion);
        }
        if (oldVersion != mVersion) {
            Editor editor = preferences.edit();
            editor.putInt(mName + VERSION_KEY, mVersion);
            editor.commit();
        }
    }
}
