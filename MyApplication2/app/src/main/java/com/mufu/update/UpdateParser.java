package com.mufu.update;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mufu.util.ParseUtils;
import com.mufu.util.Slog;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.proxy.IUpdateParser;

import static com.mufu.common.SettingsActivity.HAD_NEW_VERSION_BROADCAST;
import static com.mufu.common.SettingsActivity.NO_NEW_VERSION_BROADCAST;

public class UpdateParser implements IUpdateParser {
    private boolean isManuallyCheck = false;
    private Context context;

    public UpdateParser(Context context, boolean isManuallyCheck){
        this.context = context;
        this.isManuallyCheck = isManuallyCheck;
    }
    @Override
    public UpdateEntity parseJson(String json) throws Exception{
    Slog.d("UpdateParser", "---------------->json: "+json);
        UpdateCheckResult result = ParseUtils.fromJson(json);
        if (result != null) {
            if (result.hasUpdate == false){
                if(isManuallyCheck == true){
                    sendBroadcast(false);
                }
                return new UpdateEntity()
                        .setHasUpdate(result.hasUpdate);
            }else {
                if(isManuallyCheck == true){
                    sendBroadcast(true);
                }
                return new UpdateEntity()
                        .setHasUpdate(result.hasUpdate)
                        .setIsIgnorable(result.isIgnorable)
                        .setVersionCode(result.versionCode)
                        .setVersionName(result.versionName)
                        .setUpdateContent(result.updateLog)
                        .setDownloadUrl(result.apkUrl)
                        .setSize(result.apkSize);
            }
        }

        return null;
    }

    private void sendBroadcast(boolean had) {
        if (had == false){
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NO_NEW_VERSION_BROADCAST));
        }else {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(HAD_NEW_VERSION_BROADCAST));
        }
    }
}
 
 
