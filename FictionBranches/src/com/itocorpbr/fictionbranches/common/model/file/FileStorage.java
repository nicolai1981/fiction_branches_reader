package com.itocorpbr.fictionbranches.common.model.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.itocorpbr.fictionbranches.common.model.Model;

public abstract class FileStorage extends Model {

    private final FileManager mFileManager;

    public FileStorage(Context context, FileManager fileManager) {
        super(fileManager);
        mFileManager = fileManager;
    }

    protected Uri getUri(String path) {
        return Uri.withAppendedPath(getUri(), path);
    }

    protected Uri getUri(String path, String name) {
        return Uri.withAppendedPath(getUri(path), name);
    }

    public File getFile(Uri uri) {
        if (!TextUtils.equals(uri.getAuthority(), getUri().getAuthority())) {
            return null;
        }
        return mFileManager.getFile(uri.getPath());
    }

    protected File getFile(String path, String name) {
        mFileManager.createFolder(getPath(path));
        return mFileManager.getFile(getPath(path), name);
    }

    protected void createFile(String path, String name, InputStream is) {
        mFileManager.createFile(getPath(path), name, is);
        addNotifyChange(getUri(path, name));
    }

    protected FileOutputStream createFileOutputStream(String path, String name) {
        return mFileManager.createFileOutputStream(getPath(path), name);
    }

    protected void deleteFile(String path, String name) {
        mFileManager.deleteFile(getPath(path), name);
        addNotifyChange(getUri(path, name));
    }

    protected boolean fileExists(String path, String name) {
        return mFileManager.fileExists(getPath(path), name);
    }

    protected File getFolder(String path) {
        return mFileManager.getFolder(getPath(path));
    }

    protected void createFolder(String path) {
        if (mFileManager.createFolder(getPath(path))) {
            addNotifyChange(getUri(path));
        }
    }

    protected void deleteFolder(String path) {
        mFileManager.deleteFolder(getPath(path));
        addNotifyChange(getUri(path));
    }

    protected boolean folderExists(String path) {
        return mFileManager.folderExists(getPath(path));
    }

    private String getPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return getModelName();
        } else {
            return getModelName() + java.io.File.separator + path;
        }
    }

    @Override
    public void onDeleteModel() {
        mFileManager.deleteFolder(getModelName());
    }

    @Override
    public void onCreateModel() {
        mFileManager.createFolder(getModelName());
    }
}
