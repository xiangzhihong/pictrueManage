package com.xzh.picturesmanager.album.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;


import com.xzh.picturesmanager.base.YmatouApplication;
import com.xzh.picturesmanager.ui.BrowseCameraActivity;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangzhihong on 2016/4/13 on 11:32.
 */
public class AlbumUtils {


    public static void openBrowseCamera(final Activity mContext, String cameraPath) {
        BrowseCameraActivity.open(mContext, cameraPath);
    }

    public static String getImagePath() {
        String pictruePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/";
        String fileName = "";
        File file = new File(pictruePath);
        if (!file.exists())
        file.mkdirs();
        fileName = pictruePath + getFileName();
        return fileName;
    }

    public static String getDCIMPath() {
        String pictruePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/";
        String  fileName = getFileName() ;
        File file = new File(pictruePath,fileName);
        if (!file.exists()) {
            file.mkdir();
        }
        fileName=file+fileName;
        return fileName;
    }



    public static Bitmap getBitmap(String srcPath, int width, int height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int be = 1;
        if (w > h && w > width) {
            be = (int) (newOpts.outWidth / width);
        } else if (w < h && h > height) {
            be = (int) (newOpts.outHeight / height);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        try {
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String saveBitmap(Bitmap bitmap) {
        String pictruePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/";
        String fileName = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            File dir = new File(pictruePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            fileName = pictruePath + "/" + getFileName();
            File file = new File(fileName);
            file.delete();
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
            MediaScannerConnection.scanFile(YmatouApplication.getInstance(),
                    new String[]{pictruePath}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    }
            );
            bos.flush();
            fos.flush();
            baos.flush();
            bos.close();
            fos.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public static String getFileName() {
        String fileName = System.currentTimeMillis() + ".jpg";
        return fileName;
    }

    public static Drawable getCompoundDrawable(@NonNull Context context, @DrawableRes int resId) {
        Drawable drawable = context.getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }

}
