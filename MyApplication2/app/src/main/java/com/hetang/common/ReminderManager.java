package com.hetang.common;

import java.util.ArrayList;
import java.util.List;

/*
 *消息管理器
 */
public class ReminderManager {
    // callback
    public interface UnreadNumChangedCallback {
        void onUnreadNumChanged(int unReadCount);
        void onNotificationUnreadChanged(int unReadCount);
    }
    
    // singleton
    private static ReminderManager instance;

    public static synchronized ReminderManager getInstance() {
        if (instance == null) {
            instance = new ReminderManager();
        }

        return instance;
    }


    private List<UnreadNumChangedCallback> unreadNumChangedCallbacks = new ArrayList<>();
    
    private ReminderManager() {
        //populate(items);
    }


    public void registerUnreadNumChangedCallback(UnreadNumChangedCallback cb) {
        if (unreadNumChangedCallbacks.contains(cb)) {
            return;
        }
        unreadNumChangedCallbacks.add(cb);
    }

    public void unregisterUnreadNumChangedCallback(UnreadNumChangedCallback cb) {
        if (!unreadNumChangedCallbacks.contains(cb)) {
            return;
        }

        unreadNumChangedCallbacks.remove(cb);
    }
    
    // interface
    public final void updateSessionUnreadNum(int unreadNum) {
        updateUnreadMessageNum(unreadNum);
    }

    private final void updateUnreadMessageNum(int unreadNum) {
        for (UnreadNumChangedCallback cb : unreadNumChangedCallbacks) {
            cb.onUnreadNumChanged(unreadNum);
        }
    }

    public final void updateNotificationUnreadNum(int unreadNum){
        for (UnreadNumChangedCallback cb : unreadNumChangedCallbacks) {
            cb.onNotificationUnreadChanged(unreadNum);
        }
    }
}
