package com.mufu.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.TypedValue;


/**
 * Created by haichao.zou on 2017/8/22.
 */

public class Utility {
    private static final String TAG = "Utility";
    public static final int MALE = 0;
    public enum TalentType {
        GUIDE, EXPERIENCE, TRAVEL, GROWTH, INTEREST, FOOD, MATCHMAKER
    }

    public enum ConsultType {
        ANSWERED, QUESTIONED, ALL
    }
    
    public enum OrderType {
        TODAY("today", 0), QUEUED("queued", 1), FINISHED("finished", 2),
        UNPAYMENT("unpayment", 3), BOOKED("booked", 4), WAITING_EVALUATION("waiting_evaluation", 5),
        MY_ALL("my_all", 9), ALL_SOLD("all", 10);

        private String _name;
        private int _type;

        OrderType(String name, int type) {
            _name = name;
            _type = type;
        }
        
        public String getName(){ return _name; };
        public int getType(){ return _type; };

        public static OrderType getOrderType(int type){
            switch (type){
                case 0:
                    return TODAY;
                case 1:
                    return QUEUED;
                case 2:
                    return FINISHED;
                case 3:
                    return UNPAYMENT;
                case 4:
                    return BOOKED;
                case 5:
                    return WAITING_EVALUATION;
                case 9:
                    return MY_ALL;
                case 10:
                    return ALL_SOLD;
                    default:
                        return TODAY;
            }
        }
    }
            
    public static boolean handleLoginResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            /*
            try {

            }catch (JSONException e){
                e.printStackTrace();
            }
            */
        }
        return false;

    }
    
    public static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
    
    public static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    
     /**
     * 获取版本号
     *  @return 当前应用的版本号 versioncode
     * */
    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            //String version = info.versionName;
            int versioncode = info.versionCode;
            return versioncode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 获取版本号
     *  @return 当前应用的版本号versionname
     * */
    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
           // int versioncode = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
     /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Drawable转换成一个Bitmap
     *
     * @param drawable drawable对象
     * @return
     */
    public static final Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    
    public static int dpToPx(Context context, int dp) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
    
     public static File drawable2File(Context context, Drawable drawable, String fileName){
        Bitmap bitmap = drawableToBitmap(drawable);
        String defaultPath = context.getFilesDir().getAbsolutePath();
        Slog.d(TAG, "------------------>defaultPath: "+defaultPath);
        File file = new File(defaultPath);
        if (!file.exists()){
            file.mkdirs();
        }

        String defaultImgPath = defaultPath + "/"+fileName;
        Slog.d(TAG, "------------------>defaultImgPath: "+defaultImgPath);
        file = new File(defaultImgPath);
         
         try {
            file.createNewFile();

            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
    
    public static File drawable2File(Context context, int resId, String fileName){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        String defaultPath = context.getFilesDir().getAbsolutePath();
        Slog.d(TAG, "------------------>defaultPath: "+defaultPath);
        File file = new File(defaultPath);
        if (!file.exists()){
            file.mkdirs();
        }

        String defaultImgPath = defaultPath + "/"+fileName;
        Slog.d(TAG, "------------------>defaultImgPath: "+defaultImgPath);
        file = new File(defaultImgPath);
        
        try {
            file.createNewFile();

            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
    
        public static void saveToSystemGallery(Context context, Bitmap bmp) {

        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "DCIM");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

         // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            //Slog.d(TAG, "----------------------->fileName: "+fileName+"   path: "+file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
    }
}
