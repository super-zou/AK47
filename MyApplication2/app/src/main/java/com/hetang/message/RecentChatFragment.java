package com.hetang.message;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hetang.util.HttpUtil;
import com.hetang.util.UserProfile;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.recent.RecentContactsCallback;
import com.netease.nim.uikit.business.recent.RecentContactsFragment;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.hetang.R;
import com.hetang.adapter.NotificationListAdapter;
import com.hetang.common.ReminderManager;
import com.hetang.util.Slog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecentChatFragment extends Fragment {
    private static final boolean isDebug = false;
    private static final String TAG = "RecentChatFragment";
    private static final int PAGE_SIZE = 6;
    private static final int LOAD_DONE = 0;
    private static final int UPDATE_DONE = 1;
    private static final String GET_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/load";
    private static final String UPDATE_NOTICE_URL = HttpUtil.DOMAIN + "?q=notice/update";

    private List<NotificationFragment.Notification> notificationList = new ArrayList<>();
    private XRecyclerView xRecyclerView;
    private int mTempSize;
    private NotificationListAdapter notificationListAdapter;
    private Context mContext;
    private RecentChatFragment.MyHandler handler;
    Typeface font;
    private RecentContactsFragment fragment;
    
    public static class Notification {
        public int nid;
        public int uid;
        public int tid;
        public UserProfile trigger;
        public String action;
        public String content;
        public int isNew = 0;
        public int type;
        public int timeStamp;
        public int id;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.session, container, false);
        //unreadCallback(convertView);
        addRecentContactsFragment();
        return convertView;
    }
    
    // 将最近联系人列表fragment动态集成进来。 开发者也可以使用在xml中配置的方式静态集成。
    private void addRecentContactsFragment() {
        fragment = new RecentContactsFragment();
        fragment.setContainerId(R.id.sessions_fragment);

       // final MainActivity activity =  getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.sessions_fragment, fragment);
        //transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
        
        fragment.setCallback(new RecentContactsCallback() {
            @Override
            public void onRecentContactsLoaded() {
                // 最近联系人列表加载完毕
            }

            @Override
            public void onUnreadCountChange(int unreadCount) {
                ReminderManager.getInstance().updateSessionUnreadNum(unreadCount);
               if (isDebug) Slog.d(TAG, "------------------------>onUnreadCountChange: "+unreadCount);
            }
            
            @Override
            public void onItemClick(RecentContact recent) {
                // 回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作
                switch (recent.getSessionType()) {
                    case P2P:
                        NimUIKit.startP2PSession(getActivity(), recent.getContactId());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public String getDigestOfAttachment(RecentContact recentContact, MsgAttachment attachment) {

                return null;
            }
            
            @Override
            public String getDigestOfTipMsg(RecentContact recent) {
                String msgId = recent.getRecentMessageId();
                List<String> uuids = new ArrayList<>(1);
                uuids.add(msgId);
                List<IMMessage> msgs = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
                if (msgs != null && !msgs.isEmpty()) {
                    IMMessage msg = msgs.get(0);
                    Map<String, Object> content = msg.getRemoteExtension();
                    if (content != null && !content.isEmpty()) {
                        return (String) content.get("content");
                    }
                }

                return null;
            }
        });
    }
    
    public void handleMessage(Message message) {
        switch (message.what){
            case LOAD_DONE:

                break;
            case UPDATE_DONE:

                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    static class MyHandler extends Handler {
        WeakReference<RecentChatFragment> recentChatFragmentWeakReference;

        MyHandler(RecentChatFragment recentChatFragment) {
            recentChatFragmentWeakReference = new WeakReference<>(recentChatFragment);
        }

        @Override
        public void handleMessage(Message message) {
            RecentChatFragment recentChatFragment = recentChatFragmentWeakReference.get();
            if (recentChatFragment != null) {
                recentChatFragment.handleMessage(message);
            }
        }
    }
}
