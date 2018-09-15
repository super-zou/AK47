package com.tongmenhui.launchak47.util;


import com.tongmenhui.launchak47.adapter.MeetDynamicsListAdapter;
import com.tongmenhui.launchak47.meet.DynamicsComment;
import com.tongmenhui.launchak47.meet.MeetDynamics;

/**
 * Created by super-zou on 18-9-9.
 */

public interface CommentDialogFragmentInterface {
    public void onCommentClick(MeetDynamics meetDynamics, DynamicsComment dynamicsComment);
    //public String getCommentText();
    //public void setCommentText(String commentText);
}
