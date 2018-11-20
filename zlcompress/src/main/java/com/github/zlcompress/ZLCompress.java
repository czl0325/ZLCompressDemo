package com.github.zlcompress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ZLCompress {
    static {
        System.loadLibrary("jpegbither");
        System.loadLibrary("bitherjni");
        System.loadLibrary("native-compress");
    }

    public static native int compressBitmap(Bitmap bit, int w, int h, int quality, byte[] fileNameBytes, boolean optimize);

    public static void huffmanCompress(Bitmap bitmap, File file) {
        compressBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), 50,
                file.getAbsolutePath().getBytes(), true);
    }

    public static void qualityCompress(Bitmap bitmap, File file) {
        qualityCompress(bitmap, 50, file);
    }
    /**
     * 1. 质量压缩
     * 			     设置bitmap options属性，降低图片的质量，像素不会减少
     * 			     第一个参数为需要压缩的bitmap图片对象，第二个参数为压缩后图片保存的位置
     * 			     设置options 属性0-100，来实现压缩
     * @param bitmap
     * @param quality 压缩质量 0-100
     * @param file
     */
    public static void qualityCompress(Bitmap bitmap, int quality, File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        try {
            FileOutputStream fs = new FileOutputStream(file);
            fs.write(baos.toByteArray());
            fs.flush();
            fs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sizeCompress(Bitmap bitmap, File file) {
        sizeCompress(bitmap, 2, file);
    }

    /**
     * 尺寸压缩
     通过缩放图片像素来减少图片占用内存大小
     * @param bitmap
     * @param ratio 尺寸压缩倍数,值越大，图片尺寸越小
     * @param file
     */
    public static void sizeCompress(Bitmap bitmap, int ratio, File file) {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth()/ratio, bitmap.getHeight()/ratio,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0,0,bitmap.getWidth()/ratio, bitmap.getHeight()/ratio);
        canvas.drawBitmap(bitmap, null, rect, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        try {
            FileOutputStream fs = new FileOutputStream(file);
            fs.write(baos.toByteArray());
            fs.flush();
            fs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sampleCompress(String inPath, File outPath) {
        sampleCompress(8, inPath, outPath);
    }

    /**
     * 采样率压缩
     * @param sampleSize 采样率
     * @param inPath
     * @param outPath
     */
    public static void sampleCompress(int sampleSize, String inPath, File outPath) {
        //数值越高，图片像素越低
        int inSampleSize = 8;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;//为true的时候不会真正加载图片，而是得到图片的宽高信息。
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(inPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        try {
            if (outPath.exists()) {
                outPath.delete();
            } else {
                outPath.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(outPath);
            fs.write(baos.toByteArray());
            fs.flush();
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
