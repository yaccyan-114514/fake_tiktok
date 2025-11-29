package com.example.fake_tiktok;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 相机功能辅助类
 * 用于处理拍照功能
 */
public class CameraActivity {
    
    private static final String AUTHORITY_SUFFIX = ".fileprovider";
    
    /**
     * 创建拍照Intent
     * @param activity 当前Activity
     * @param authority FileProvider的authority（通常是包名 + ".fileprovider"）
     * @return 拍照Intent，如果创建失败返回null
     */
    public static Intent createTakePictureIntent(Activity activity, String authority) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // 检查是否有相机应用可用
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) == null) {
            android.util.Log.e("CameraActivity", "没有可用的相机应用");
            return null;
        }
        
        // 创建临时图片文件
        File photoFile = null;
        try {
            photoFile = createImageFile(activity);
        } catch (IOException ex) {
            android.util.Log.e("CameraActivity", "创建图片文件失败", ex);
            return null;
        }
        
        if (photoFile == null) {
            android.util.Log.e("CameraActivity", "无法创建图片文件");
            return null;
        }
        
        // 获取图片URI
        Uri photoURI = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0及以上使用FileProvider
                photoURI = FileProvider.getUriForFile(
                    activity,
                    authority,
                    photoFile
                );
            } else {
                // Android 7.0以下直接使用file://
                photoURI = Uri.fromFile(photoFile);
            }
        } catch (Exception e) {
            android.util.Log.e("CameraActivity", "获取图片URI失败", e);
            return null;
        }
        
        // 将URI添加到Intent中
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        
        // 授予临时权限
        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        // 保存文件路径到Intent的extra中，方便后续获取
        takePictureIntent.putExtra("photo_path", photoFile.getAbsolutePath());
        
        return takePictureIntent;
    }
    
    /**
     * 创建临时图片文件
     * @param activity 当前Activity
     * @return 创建的图片文件
     * @throws IOException 如果创建失败
     */
    private static File createImageFile(Activity activity) throws IOException {
        // 创建文件名（使用时间戳）
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        
        // 获取应用的外部文件目录（不需要权限）
        File storageDir = activity.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        
        // 如果目录不存在，创建它
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        // 创建临时文件
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
        
        android.util.Log.d("CameraActivity", "创建图片文件: " + image.getAbsolutePath());
        
        return image;
    }
    
    /**
     * 从Intent中获取拍照后的图片URI
     * @param activity 当前Activity
     * @param intent 拍照返回的Intent
     * @param authority FileProvider的authority
     * @return 图片URI，如果获取失败返回null
     */
    public static Uri getPhotoUri(Activity activity, Intent intent, String authority) {
        if (intent == null) {
            return null;
        }
        
        // 尝试从extra中获取文件路径
        String photoPath = intent.getStringExtra("photo_path");
        if (photoPath != null) {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        return FileProvider.getUriForFile(
                            activity,
                            authority,
                            photoFile
                        );
                    } else {
                        return Uri.fromFile(photoFile);
                    }
                } catch (Exception e) {
                    android.util.Log.e("CameraActivity", "从文件路径获取URI失败", e);
                }
            }
        }
        
        // 如果没有文件路径，尝试从Intent的data中获取（某些相机应用会返回缩略图）
        Uri photoURI = intent.getData();
        if (photoURI != null) {
            return photoURI;
        }
        
        // 尝试从EXTRA_OUTPUT中获取
        photoURI = (Uri) intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        if (photoURI != null) {
            return photoURI;
        }
        
        android.util.Log.w("CameraActivity", "无法从Intent中获取图片URI");
        return null;
    }
    
    /**
     * 检查是否有相机权限
     * @param activity 当前Activity
     * @return true如果有权限，false如果没有权限
     */
    public static boolean hasCameraPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(android.Manifest.permission.CAMERA) == 
                   android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 6.0以下不需要运行时权限
    }
    
    /**
     * 请求相机权限
     * @param activity 当前Activity
     * @param requestCode 请求码
     */
    public static void requestCameraPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                new String[]{android.Manifest.permission.CAMERA},
                requestCode
            );
        }
    }
}
