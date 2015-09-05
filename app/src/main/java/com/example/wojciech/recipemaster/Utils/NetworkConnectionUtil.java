package com.example.wojciech.recipemaster.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.example.wojciech.recipemaster.model.Recipe;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Wojciech on 2015-09-04.
 */
public class NetworkConnectionUtil extends AsyncTask<String, Void, String> {
    private final static String TAG = NetworkConnectionUtil.class.getSimpleName();
    private NetworkConnectionCallback networkConnectionCallback;
    public interface NetworkConnectionCallback{
        void onContentDownload(String content, int errorCode);
    }
    public NetworkConnectionUtil(Activity activity){
        try {
            networkConnectionCallback = (NetworkConnectionCallback)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NetworkConnectionCallback");
        }
    }
    @Override
    protected String doInBackground(String... urls) {

        // params comes from the execute() call: params[0] is the url.
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if(TextUtils.isEmpty(result))
            networkConnectionCallback.onContentDownload("Unable to retrieve web page. URL may be invalid.",1);
        else
            networkConnectionCallback.onContentDownload(result,0);
    }
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is, len);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, "UTF-8"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        stream.close();
        return sb.toString();
    }

}
