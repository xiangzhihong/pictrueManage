package com.xzh.picturesmanager.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;


/**
 * 图片工具类
 */
public class ImageUtils {

  public static String pictruePath = Environment.getExternalStorageDirectory()
            + "/image";
    public static String getImagePath() {
        String pictruePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/";
        String fileName = "";
        File file = new File(pictruePath);
        if (!file.exists())
            file.mkdirs();
        fileName = pictruePath + getFileName();
        return fileName;
    }
    public static String getFileName() {
        String fileName = System.currentTimeMillis() + ".jpg";
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

    public static String savePictrue(Bitmap bitmap) {
        String fileName=null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            File dir = new File(pictruePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            fileName = pictruePath + "/" + getTempFileName() + ".png";
            File file = new File(fileName);
            file.delete();
            if (!file.exists()) {
                file.createNewFile();
            }
            if (DeviceUtil.isExistSDCard()){
                FileOutputStream  fos = new FileOutputStream(file);
                BufferedOutputStream  bos = new BufferedOutputStream(fos);
                bos.write(byteArray);
                bos.flush();
                fos.flush();
                baos.flush();
                bos.close();
                fos.close();
                baos.close();
            }else {
                Utils.shortToast("SD卡不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
      return fileName;
    }

    public static String saveBitmap(Bitmap photoBitmap) {
        File dir = new File(pictruePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photoFile = new File(pictruePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(photoFile);
            if (photoBitmap != null) {
                if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        fileOutputStream)) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return photoFile.getAbsolutePath()+getTempFileName();
    }

    public static String getTempFileName() {
        return System.currentTimeMillis()+".jpg";
    }

    //从文件中读取Bitmap
    public static Bitmap decodeBitmapWithOrientation(String pathName, int width, int height) {
        return decodeBitmapWithSize(pathName, width, height, false);
    }

    public static Bitmap decodeBitmapWithOrientationMax(String pathName, int width, int height) {
        return decodeBitmapWithSize(pathName, width, height, true);
    }

    private static Bitmap decodeBitmapWithSize(String pathName, int width, int height,
                                               boolean useBigger) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inInputShareable = true;
        options.inPurgeable = true;
        BitmapFactory.decodeFile(pathName, options);
        int decodeWidth = width, decodeHeight = height;
        final int degrees = getImageDegrees(pathName);
        if (degrees == 90 || degrees == 270) {
            decodeWidth = height;
            decodeHeight = width;
        }

        if (useBigger) {
            options.inSampleSize = (int) Math.min(((float) options.outWidth / decodeWidth),
                    ((float) options.outHeight / decodeHeight));
        } else {
            options.inSampleSize = (int) Math.max(((float) options.outWidth / decodeWidth),
                    ((float) options.outHeight / decodeHeight));
        }
        options.inJustDecodeBounds = false;
        Bitmap sourceBm = BitmapFactory.decodeFile(pathName, options);
        return imageWithFixedRotation(sourceBm, degrees);
    }

    public static int getImageDegrees(String pathName) {
        int degrees = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(pathName);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degrees = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degrees = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degrees;
    }

    public static Bitmap imageWithFixedRotation(Bitmap bm, int degrees) {
        if (bm == null || bm.isRecycled())
            return null;

        if (degrees == 0)
            return bm;

        final Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        if (result != bm)
            bm.recycle();
        return result;

    }
    public static Bitmap byteToBitmap(byte[] imgByte) {
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        input = new ByteArrayInputStream(imgByte);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
        if (imgByte != null) {
            imgByte = null;
        }

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

}
