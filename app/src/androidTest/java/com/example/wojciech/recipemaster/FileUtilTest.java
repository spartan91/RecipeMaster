package com.example.wojciech.recipemaster;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.test.InstrumentationTestCase;

import com.example.wojciech.recipemaster.utils.DownloadUtil;
import com.example.wojciech.recipemaster.utils.FileUtil;

import java.io.File;

/**
 * Created by Wojciech on 2015-09-05.
 */
public class FileUtilTest extends InstrumentationTestCase {
    public void testFileUtil_SaveImageToPublicCameraDir() throws Exception {
        final String cameraFolder = "Camera";
        final String photoFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + cameraFolder + "/";
        final String fileName = "TEST.jpg";
        File file = new File(photoFolder + fileName);
        if (file.exists()) {
            file.delete();
        }
        //file.createNewFile();
        Bitmap bm = DownloadUtil.downloadBitmap("http://mooduplabs.com/test/pizza1.jpg");// BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.main);
        FileUtil.saveImageToPublicCameraDir(bm, fileName);

        file = new File(photoFolder + fileName);
        assertEquals(true, file.exists());
        if (file.exists()) {
            file.delete();
        }


    }
}
