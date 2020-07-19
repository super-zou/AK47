package com.mufu.message;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.mufu.contacts.ChatActivity;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMOfflinePushNotification;
import com.tencent.imsdk.friendship.TIMCheckFriendResult;
import com.tencent.imsdk.log.QLog;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.mufu.common.MyApplication.getContext;

/** @deprecated */
@Deprecated
public class TIMChatNotification extends TIMOfflinePushNotification {

    private static final String TAG = TIMCheckFriendResult.class.getSimpleName();
    private String tag;
    private ChatInfo chatInfo;
    private TIMMessage message;

    public TIMChatNotification(Context context, TIMMessage msg){
        super(context, msg);
        message = msg;
        tag = msg.getConversation().getPeer();
    }

    /** @deprecated */
    @Deprecated
    public void doNotify(Context context, int iconID) {
        NotificationManager manager = (NotificationManager)context.getApplicationContext().getSystemService("notification");
        if (manager == null) {
            QLog.e(this.TAG, "get NotificationManager failed");
        } else {
            Notification.Builder builder;
            String tickerStr;
            if (Build.VERSION.SDK_INT >= 26) {
                tickerStr = "FakeNotification";
                String channelName = "FakeNotificationName";
                builder = new Notification.Builder(context, tickerStr);
                NotificationChannel channel = new NotificationChannel(tickerStr, channelName, 4);
                manager.createNotificationChannel(channel);
            } else {
                builder = new Notification.Builder(context);
            }

            tickerStr = "收到一条新消息";
            builder.setTicker(tickerStr);
            builder.setContentTitle(this.getTitle());
            builder.setContentText(this.getContent());
            builder.setSmallIcon(iconID);
            builder.setAutoCancel(true);
            builder.setDefaults(-1);
            if (getSound() != null) {
                builder.setDefaults(6);
                builder.setSound(getSound());
            }

            //Intent launch = context.getApplicationContext().getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (chatInfo == null){
                chatInfo = new ChatInfo();
            }
            chatInfo.setType(TIMConversationType.C2C);
            chatInfo.setId(String.valueOf(message.getSender()));
            chatInfo.setChatName(message.getSenderNickname());

            //chatInfo.setId();
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("CHAT_INFO", chatInfo);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pi = PendingIntent.getActivity(context,(int) SystemClock.uptimeMillis() ,intent ,FLAG_UPDATE_CURRENT );
            builder.setContentIntent(pi);
            manager.notify(this.tag, 520, builder.build());
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("title: ").append(getTitle()).append("|content: ")
                .append(getContent()).append("|sid: ").append(getConversationId())
                .append("|sender: ").append(this.getSenderIdentifier())
                .append("|senderNick: ").append(this.getSenderNickName()).append("|tag: ")
                .append(tag).append("|isValid: ").append(isValid());
        return builder.toString();
    }
}
