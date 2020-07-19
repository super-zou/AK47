package com.mufu.common;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.mufu.util.Slog;

import java.io.File;

public class DownloadService extends IntentService {
    private String TAG = "DownloadService";
    public static final String BROADCAST_ACTION =
            "com.example.android.threadsample.BROADCAST";
    public static final String EXTENDED_DATA_STATUS =
            "com.example.android.threadsample.STATUS";

    private LocalBroadcastManager mLocalBroadcastManager;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        //获取下载地址
        Slog.d(TAG, "----------------------------->onHandleIntent");
        String url = intent.getDataString();
        Log.i(TAG,url);
        //获取DownloadManager对象
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

       //设置下载的路径
        File file = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOWNLOADS);
        request.setDestinationUri(Uri.fromFile(file));
        
        //设置网络下载环境为wifi
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //设置显示通知栏，下载完成后通知栏自动消失
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);
        //设置通知栏标题
        request.setTitle("下载");
        request.setDescription("应用正在下载");
        //request.setAllowedOverRoaming(true);
        request.allowScanningByMediaScanner();
        request.setMimeType("application/vnd.android.package-archive");

        //获得唯一下载id
        long requestId = downloadManager.enqueue(request);
        Slog.d(TAG, "------------------->requestId: "+requestId);
        //将id放进Intent
        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(EXTENDED_DATA_STATUS,requestId);
        
        // 使用方法 1.初始化
        TestFileObserver testFileObserver = new TestFileObserver(file.getAbsolutePath());
        // 2.开始监听
        testFileObserver.startWatching();

    }

    /**
     * 下载前先移除前一个任务，防止重复下载
     *
     * @param downloadId
     */
    public void clearCurrentTask(long downloadId) {
        DownloadManager dm = (DownloadManager) MyApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            dm.remove(downloadId);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
    
    class TestFileObserver extends FileObserver {
        // path 为 需要监听的文件或文件夹
        public TestFileObserver(String path) { super(path,FileObserver.ALL_EVENTS); }
        @Override
        public void onEvent(int event, String path) {
            // 如果文件修改了 打印出文件相对监听文件夹的位置
            /*
            if(event==FileObserver.MODIFY){
                Log.d("edong",path);
            } */
            Slog.d(TAG, "--------------------------------->TestFileObserver onEvent");
        }
    }
}
