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
    
    public enum ExperienceType {
        LANGUAGE_CULTURE("language_culture", 0), PARTY_SALON("party_salon", 1), FRIENDSHIP("friendship", 2),
        NATURAL_OUTDOOR("natural_outdoor", 3), HUMANITY_ART("humanity_art", 4), NGO_PUBLIC_GOOD("ngo_public_good", 5),
        THEATRE_PERFORMANCE("theatre_performance", 6), LEARNING_GROWTH("learning_growth", 7), HOBBY("hobby", 8);
        
        private String _name;
        private int _type;

        ExperienceType(String name, int type) {
            _name = name;
            _type = type;
        }

        public String getName(){ return _name; };
        public int getType(){ return _type; };
        
        public static ExperienceType getExperienceType(int type){
            switch (type){
                case 0:
                    return LANGUAGE_CULTURE;
                case 1:
                    return PARTY_SALON;
                case 2:
                    return FRIENDSHIP;
                case 3:
                    return NATURAL_OUTDOOR;
                case 4:
                    return HUMANITY_ART;
                case 5:
                    return NGO_PUBLIC_GOOD;
                case 6:
                    return THEATRE_PERFORMANCE;
                case 7:
                    return LEARNING_GROWTH;
                case 8:
                    return HOBBY;
                default:
                    return LANGUAGE_CULTURE;
            }
        }
    }

    public enum ConsultType {
        ANSWERED, QUESTIONED, ALL
    }
    
    public enum OrderClass {
        NORMAL, BLOCK_BOOKING
    }
    
    public enum OrderStatus {
        TO_BE_PAID, PAID, FINISHED, EVALUATED, APPLYING_REFUND, REFUNDED
    }
    
    public enum OrderType {
        WAIT_PAYMENT("wait_payment", 0), TODAY("today", 1), QUEUED("queued", 2), FINISHED("finished", 3),
        MY_UNPAYMENT("unpayment", 4), BOOKED("booked", 5), WAITING_EVALUATION("waiting_evaluation", 6),
        APPLYING_REFUND("applying_refund", 7), REFUNDED("refunded", 8),
        MY_ALL_SOLD("my_all_sold", 9), MY_ALL("my_all", 10), ALL_SOLD("all", 11);

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
                    return WAIT_PAYMENT;
                case 1:
                    return TODAY;
                case 2:
                    return QUEUED;
                case 3:
                    return FINISHED;
                case 4:
                    return MY_UNPAYMENT;
                case 5:
                    return BOOKED;
                case 6:
                    return WAITING_EVALUATION;
                case 7:
                    return APPLYING_REFUND;
                case 8:
                    return REFUNDED;
                case 9:
                    return MY_ALL_SOLD;
                case 10:
                    return MY_ALL;
                case 11:
                    return ALL_SOLD;
                    default:
                        return TODAY;
            }
        }
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
