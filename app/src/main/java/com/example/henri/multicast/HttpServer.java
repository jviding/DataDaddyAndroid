package com.example.henri.multicast;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import com.example.henri.multicast.NanoHTTPD;
//import com.example.henri.multicast.ServerRunner;

/**
 * Created by Jasu on 12.3.2016.
 */
public class HttpServer extends NanoHTTPD {

    private final int port;
    private String homeDir;

    private ItemAdded itemAdded;
    public interface ItemAdded {
        void itemAdded(String downloadable, String address);
    }

    public HttpServer(int port) throws IOException {
        super(port);
        this.port = port;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = Environment.getExternalStorageDirectory().toString()+File.separator+homeDir;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        Map<String, String> params = session.getParms();
        String uri = session.getUri();
        String ip = session.getRemoteIpAddress();
        if (Method.PUT.equals(method)) {
            itemAdded.itemAdded(uri, ip);
        } else if (Method.GET.equals(method)) {
            try {
                InputStream data = new FileInputStream(homeDir+uri);
                Long fileSize = new File(homeDir+uri).length();
                String mediaType = getMimeTypeForFile(homeDir+uri);
                return new NanoHTTPD.Response(Response.Status.OK, mediaType, data, fileSize);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        Log.d("HTTP",method+": '"+uri+"'");
        String msg = "Hello World!";
        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, msg);
    }

    public void downloadFileWithGet(String address, String file) {
        new AsyncTask<String,Void,Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    URL url = new URL("http://" + params[0] + ":" + params[1] + params[2]);
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    Log.d("Error", "before");
                    InputStream in = new BufferedInputStream(httpCon.getInputStream());
                    Log.d("Error","after");
                    // download the file
                    String filePath = homeDir+params[2];
                    File f = new File(filePath);
                    f.getParentFile().mkdirs();
                    OutputStream out = new FileOutputStream(f);
                    byte data[] = new byte[4096];
                    int count;
                    while ((count = in.read(data)) != -1) {
                        out.write(data, 0, count);
                    }
                    in.close();
                    out.close();
                    httpCon.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(address, Integer.toString(port), file);
    }

    public void setCallbackForNewDownloadable(ItemAdded itemAdded) {
        this.itemAdded = itemAdded;
    }

    public void sendFilesWithPut(String address, String file) {
        new AsyncTask<String,Void,Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    URL url = new URL("http://" + params[0] + ":" + params[1]);
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setDoOutput(true);
                    httpCon.setRequestMethod("PUT");
                    OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
                    out.write(params[2]);
                    out.close();
                    System.err.println(httpCon.getResponseCode());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(address, Integer.toString(port), file);
    }

}
