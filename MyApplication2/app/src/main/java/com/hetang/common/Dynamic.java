package com.hetang.common;

import com.hetang.archive.ArchiveActivity;
import com.hetang.home.HomeFragment;
import com.hetang.meet.MeetSingleGroupFragment;
import com.hetang.meet.UserMeetInfo;
import com.hetang.util.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by haichao.zou on 2017/11/20.
 */
 
 public class Dynamic extends UserProfile {

    private long did;
    private int type;
    private int relatedId;
    private String action;
    private long created;
    private int praisedDynamicsCount;
    private int praisedDynamics;
    private int commentCount = 0;
    private String content;
    private String dynamic_picture = "";
    public UserProfile relatedUerProfile;
    public Dynamic relatedContent;
    public UserMeetInfo relatedMeetContent;
    public MeetSingleGroupFragment.SingleGroup relatedSingleGroupContent;
    public HomeFragment.BackgroundDetail backgroundDetail;

    public long getDid() {
        return did;
    }
    
    public void setDid(long did) {
        this.did = did;
    }

    public int getType() { return type;}
    public void setType(int type){ this.type = type; }

    public int getRelatedId(){ return relatedId; }
    public void setRelatedId(int relatedId) { this.relatedId = relatedId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedString() {
        Date date = new Date(created * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(date);
    }

    public void setCreatedString(long created) {
        this.created = created;
    }

    public int getPraisedDynamicsCount() {
        return praisedDynamicsCount;
    }
    
    public void setPraisedDynamicsCount(int praiseCount) {
        this.praisedDynamicsCount = praiseCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getDynamicPicture() {
        return dynamic_picture;
    }
    
    public void setDynamicPicture(String dynamic_picture) {
        if (dynamic_picture != null && !dynamic_picture.equals("null")) {
            this.dynamic_picture = dynamic_picture;
        }
    }

    public int getPraisedDynamics() {
        return praisedDynamics;
    }

    public void setPraisedDynamics(int praised) {
        this.praisedDynamics = praised;
    }


}