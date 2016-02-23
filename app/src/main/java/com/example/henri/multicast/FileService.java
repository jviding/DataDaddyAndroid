package com.example.henri.multicast;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jasu on 21.2.2016.
 */
public class FileService {

    private final String homeDir;
    private final String basePath;

    public FileService(String homeDir) {
        this.homeDir = homeDir;
        this.basePath = getExtStorageAddr()+File.separator+homeDir;
        if (!isExternalStorageWritable()) {
            Log.d("Files", "External memory unreachable");
        } else {
            ifFirstUseCreateDir();
        }
    }

    /* Returns external storage state*/
    private String getExtStorageAddr() {
        return Environment.getExternalStorageDirectory().toString();
    }

    /* Creates a list of files and folders found from given path */
    public ArrayList<String> createFileView(String path) {
        ArrayList files = new ArrayList();
        File f = new File(getAbsolutePath(path));
        File file[] = f.listFiles();
        for (int i=0; i<file.length; i++) {
            if (file[i].isDirectory()) {
                files.add(path+File.separator+file[i].getName());
            }
        }
        for (int a=0; a<file.length; a++) {
            if (!file[a].isDirectory()) {
                files.add(path+File.separator+file[a].getName());
            }
        }
        return files;
    }

    /* Checks if app's home directory exists. If not, creates it. */
    private void ifFirstUseCreateDir() {
        File directory = new File(basePath);
        if (directory.exists()) {
            return;
        }
        if (!directory.mkdirs()) {
            Log.d("Files", "Failed to create home directory");
        }
    }

    private String getAbsolutePath(String path) {
        if (path.length() > 0 && !(path.charAt(0) + "").equals(File.separator)) {
            path += File.separator + path;
        }
        return basePath+path;
    }

    /* Checks if destination is a directory or a file */
    public boolean checkIfDir(String path) {
        File f = new File(getAbsolutePath(path));
        if (f.isDirectory()) {
            return true;
        }
        return false;
    }

    /* Moves one step down the given path */
    public String pathOneStepDown(String path) {
        if (path.length() > 1 && (path.charAt(path.length()-1)+"").equals(File.separator)) {
            path = path.substring(0, path.length()-1);
        }
        for (int i=path.length()-1; i>=0; i--) {
            if (File.separator.equals(path.charAt(i)+"")) {
                return path.substring(0,i);
            }
        }
        return path;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
