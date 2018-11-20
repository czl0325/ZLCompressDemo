package com.github.zlcompressdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.github.zlcompress.ZLCompress;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {
    private static final int CROP_PHOTO = 2;
    private Uri imageUri;
    private ImageView picture;
    String[] mPermissionList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final int REQUEST_PICK_IMAGE = 11101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        picture = findViewById(R.id.image);
    }

    public void openGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        if(data != null) {
                            Uri uri = data.getData();
                            imageUri = uri;
                            Cursor cursor = getContentResolver().query(uri, null, null, null,null);
                            if (cursor != null && cursor.moveToFirst()) {
                                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                                Log.e("czl",path);
                                File inFile = new File(path);
                                Log.e("czl","文件初始大小="+inFile.length());
                            }
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
                                    .openInputStream(imageUri));
                            picture.setImageBitmap(bitmap);
                            int result = ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE);
                            if (result != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PICK_IMAGE);
                            } else {
                                String outPath = Environment.getExternalStorageDirectory()+"/1.jpg";
                                File outFile = new File(outPath);
                                ZLCompress.huffmanCompress(bitmap, outFile);
                                Log.e("czl","文件压缩后大小="+outFile.length());
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (grantResults.length > 0) {
                String outPath = Environment.getExternalStorageDirectory()+"/1.jpg";
                File outFile = new File(outPath);
                picture.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(picture.getDrawingCache());
                picture.setDrawingCacheEnabled(false);
                ZLCompress.huffmanCompress(bitmap, outFile);
                Log.e("czl","文件压缩后大小="+outFile.length());
            }
        }
    }
}
