package com.example.henri.multicast;

import android.util.Log;

import java.io.IOException;
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

    public HttpServer(int port) throws IOException {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.d("HTTP",method+": '"+uri+"'");

        String msg = "Hello World!";
        Map<String, String> params = session.getParms();

        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, msg);
    }

    public void SendFilesWithPut(final ArrayList<String> users, final ArrayList<String> files) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String user : users) {
                    try {
                        URL url;
                        if ((""+user.charAt(0)).equals("/")) {
                            url = new URL("http:/" + user);
                        } else {
                            url = new URL("http://" + user);
                        }
                        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                        httpCon.setDoOutput(true);
                        httpCon.setRequestMethod("PUT");
                        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
                        for (String file : files) {
                            out.write(file);
                        }
                        out.close();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
