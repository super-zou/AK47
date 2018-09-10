package com.tongmenhui.launchak47.meet;

import android.widget.ArrayAdapter;

import com.tongmenhui.launchak47.util.CommentDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamics extends MeetMemberInfo {

    private long aid;
    private long created;
    private int praisedDynamicsCount;
    private int praisedDynamics;
    private int commentCount;
    private String content;
    private String activity_picture = "";
    public List<DynamicsComment> dynamicsCommentList = new ArrayList<>();


    public long getAid(){
        return aid;
    }
    public void setAid(long aid){
        this.aid = aid;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getCreated(){
        Date date = new Date(created*1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(date);
    }
    public void setCreated(long created){
        this.created = created;
    }
    public void setPraisedDynamicsCount(int praiseCount){
        this.praisedDynamicsCount = praiseCount;
    }
    public int getPraisedDynamicsCount(){
        return praisedDynamicsCount;
    }
    public void setCommentCount(int commentCount){
        this.commentCount = commentCount;
    }
    public int getCommentCount(){
        return commentCount;
    }
    public String getActivityPicture(){
         return activity_picture;
    }
    public void setActivityPicture(String activity_picture){
        if(activity_picture != null && !activity_picture.equals("")){
            this.activity_picture = activity_picture;
        }
    }
    public void addComment(DynamicsComment dynamicsComment){
        dynamicsCommentList.add(dynamicsComment);
    }
    public List<DynamicsComment> getComment(){
        return dynamicsCommentList;
    }

    public void setPraisedDynamics(int praised){
        this.praisedDynamics = praised;
    }

    public int getPraisedDynamics(){
        return praisedDynamics;
    }


}
