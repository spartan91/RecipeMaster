package com.example.wojciech.recipemaster.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Wojciech on 2015-09-05.
 */
public class FileUtil {
    private final static String CAMERA_FOLDER = "Camera";
    public final static String IMAGE_DESTINATION_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + CAMERA_FOLDER + "/";

    public static boolean saveImageToPublicCameraDir(Bitmap bitmap, String fileName) {
        if (bitmap != null && !TextUtils.isEmpty(fileName))
            try {
                if (!checkifImageExists(fileName)) {

                    FileOutputStream fos;

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    File file = new File(IMAGE_DESTINATION_PATH + fileName);
                    try {
                        file.createNewFile();
                        fos = new FileOutputStream(file);
                        fos.write(bytes.toByteArray());
                        fos.close();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return false;
    }

    public static boolean saveImageToPublicCameraDir(Drawable drawable, String fileName) {
        if (drawable != null && !TextUtils.isEmpty(fileName))
            return saveImageToPublicCameraDir(drawableToBitmap(drawable), fileName);
        return false;
    }

    public static boolean checkifImageExists(String imageName) throws Exception {
        if (TextUtils.isEmpty(imageName)) throw new Exception("imageName must be specified");
        File file = FileUtil.getImage("/" + imageName);
        if (file != null){
            String path = file.getAbsolutePath();

        try {
            Bitmap b = BitmapFactory.decodeFile(path);

            if (b == null) return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
        return true;
    }

    public static File getImage(String imageName) {

        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            mediaImage = new File(IMAGE_DESTINATION_PATH + imageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


}
