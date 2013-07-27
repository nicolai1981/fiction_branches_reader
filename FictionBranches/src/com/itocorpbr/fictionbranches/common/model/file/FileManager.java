package com.itocorpbr.fictionbranches.common.model.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

import com.itocorpbr.fictionbranches.common.model.ModelManager;

public class FileManager extends ModelManager {

    private File mBaseFolder;

    public FileManager(Context context, String name, int version) {
        super(context, name, version);
    }

    private File getBaseFolder() {
        if (mBaseFolder == null) {
            mBaseFolder = getContext().getDir(getName(), Context.MODE_PRIVATE);
            checkVersion();
        }
        return mBaseFolder;
    }

    public File getFolder(String path) {
        if (Log.ENABLED) Log.get().method();

        File baseFolder = getBaseFolder();
        return new java.io.File(baseFolder.getAbsolutePath() + java.io.File.separator + path);
    }

    public boolean folderExists(String path) {
        if (Log.ENABLED) Log.get().method();

        return getFolder(path).exists();
    }

    public boolean createFolder(String path) {
        if (Log.ENABLED) Log.get().method();

        File folder = getFolder(path);
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                Log.get().e("Could not create folder: " + path);
            }
            return true;
        }
        return false;
    }

    public void deleteFolder(String path) {
        if (Log.ENABLED) Log.get().method();

        File folder = getFolder(path);
        deleteFolder(folder);
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    if (!file.delete()) {
                        Log.get().e("Could not delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!folder.delete()) {
            Log.get().e("Could not delete folder: " + folder.getAbsolutePath());
        }
    }

    public File getFile(String path, String name) {
        if (Log.ENABLED) Log.get().method();

        File baseFolder = getBaseFolder();
        return new java.io.File(baseFolder.getAbsolutePath() + java.io.File.separator + path + java.io.File.separator + name);
    }

    public File getFile(String path) {
        if (Log.ENABLED) Log.get().method();

        File baseFolder = getBaseFolder();
        return new java.io.File(baseFolder.getAbsolutePath() + java.io.File.separator + path);
    }

    public boolean fileExists(String path, String name) {
        if (Log.ENABLED) Log.get().method();

        return getFile(path, name).exists();
    }

    public void deleteFile(String path, String name) {
        if (Log.ENABLED) Log.get().method();

        File file = getFile(path, name);
        if (!file.delete()) {
            Log.get().e("Could not delete file: " + file.getAbsolutePath());
        }
    }

    public FileOutputStream createFileOutputStream(String path, String name) {
        if (Log.ENABLED) Log.get().method();

        FileOutputStream os = null;
        createFolder(path);
        File file = getFile(path, name);
        if (file.exists()) {
            if (!file.delete()) {
                Log.get().e("Could not delete file: " + file.getAbsolutePath());
            }
        }
        try {
            os = new FileOutputStream(file);
        } catch (IOException e) {
            if (Log.ENABLED) Log.get().e(e);
        }
        return os;
    }

    public boolean createFile(String path, String name, InputStream is) {
        if (Log.ENABLED) Log.get().method();

        FileOutputStream fos = createFileOutputStream(path, name);
        if (fos == null) {
            return false;
        }
        OutputStream os = null;
        byte[] buffer = new byte[1024];
        int len;
        try {
            os = new BufferedOutputStream(fos);
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            if (Log.ENABLED) Log.get().e(e);
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                if (Log.ENABLED) Log.get().e(e);
                return false;
            }
        }
        return true;
    }
}
