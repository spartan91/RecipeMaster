package com.example.wojciech.recipemaster.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Wojciech on 2015-09-05.
 */
public class DownloadUtil extends AsyncTask<String, Void, String> {
    private final static String TAG = NetworkConnectionUtil.class.getSimpleName();
    private DownloadUtilCallback downloadUtilCallback;

    public interface DownloadUtilCallback {
        void onImageDownload(String imageDownloaded);
    }

    public DownloadUtil(Activity activity) {
        try {
            downloadUtilCallback = (DownloadUtilCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DownloadUtilCallback");
        }
    }

    @Override
    protected String doInBackground(String... url) {


        try {
            Bitmap bitmap = downloadBitmap(url[0]);
            FileUtil.saveImageToPublicCameraDir(bitmap, FilenameUtils.getName(url[0]));
            return FilenameUtils.getName(url[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        downloadUtilCallback.onImageDownload(result);

    }


    public static Bitmap downloadBitmap(String uRl) {
        try {
            URL uRL = new URL(uRl);
            if (uRL != null) {
                return BitmapFactory.decodeStream(uRL.openConnection().getInputStream());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long downloadWithDownloadManager(String name, String fullname, String uRl, Context context) {
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(fullname) && context != null) {
            final String cameraFolder = "Camera";
            final String photoFolder = Environment.DIRECTORY_DCIM + "/" + cameraFolder + "/";

            try {
                if (!FileUtil.checkifImageExists(fullname)) {

                    DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

                    Uri downloadUri = Uri.parse(uRl);
                    DownloadManager.Request request = new DownloadManager.Request(
                            downloadUri);

                    request.setAllowedNetworkTypes(
                            DownloadManager.Request.NETWORK_WIFI
                                    | DownloadManager.Request.NETWORK_MOBILE)
                            .setAllowedOverRoaming(false).setTitle(name)
                            .setDescription("Recipe " + name)
                            .setDestinationInExternalPublicDir("", photoFolder + fullname);

                    return mgr.enqueue(request);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;

    }
}
