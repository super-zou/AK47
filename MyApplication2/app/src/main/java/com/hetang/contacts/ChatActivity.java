package com.hetang.contacts;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.hetang.R;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private ChatInfo mChatInfo;
    private ChatFragment mChatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chat(getIntent());
        /*
        // 从布局文件中获取聊天面板
        ChatLayout chatLayout = findViewById(R.id.chat_layout);
        // 单聊面板的默认 UI 和交互初始化
        chatLayout.initDefault();
        ChatInfo chatInfo = (ChatInfo) getIntent().getSerializableExtra("CHAT_INFO");
        Slog.d(TAG, "----------------------------------TIMConversationType: "+chatInfo.getType());
        chatLayout.setChatInfo(chatInfo);
        */
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        chat(intent);
    }

    private void chat(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mChatInfo = (ChatInfo) bundle.getSerializable("CHAT_INFO");
            if (mChatInfo == null) {
                return;
            }else {
                mChatFragment = new ChatFragment();
                mChatFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.empty_view, mChatFragment).commitAllowingStateLoss();
            }
        }
    }
}
