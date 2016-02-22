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

    public FileService(String homeDir) {
        this.homeDir = homeDir;
        if (!isExternalStorageWritable()) {
            Log.d("Files", "External memory unreachable");
        } else {
            ifFirstUseCreateDir();
        }
    }

    /* Creates a list of files and folders found from given path */
    public ArrayList<String> createFileView(String path) {
        ArrayList files = new ArrayList();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i<file.length; i++) {
            if (file[i].isDirectory()) {
                files.add(file[i].getName());
            }
        }
        for (int a=0; a<file.length; a++) {
            if (!file[a].isDirectory()) {
                files.add(file[a].getName());
            }
        }
        return files;
    }

    /* Checks if app's home directory exists. If not, creates it. */
    public void ifFirstUseCreateDir() {
        File directory = new File(Environment.getExternalStorageDirectory()+File.separator+homeDir);
        if (directory.exists()) {
            return;
        }
        if (!directory.mkdirs()) {
            Log.d("Files", "Failed to create home directory");
        }
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
