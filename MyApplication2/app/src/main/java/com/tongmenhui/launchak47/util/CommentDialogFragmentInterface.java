package com.tongmenhui.launchak47.util;


import com.tongmenhui.launchak47.meet.MeetDynamics;

/**
 * Created by super-zou on 18-9-9.
 */

public interface CommentDialogFragmentInterface {
    public void onCommentClick(MeetDynamics meetDynamics, int type, String name, long uid);
    //public String getCommentText();
    //public void setCommentText(String commentText);
}
