package com.tongmenhui.launchak47.meet;

import android.widget.ArrayAdapter;

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
    private int praiseCount;
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
    public void setPraiseCount(int praiseCount){
        this.praiseCount = praiseCount;
    }
    public int getPraiseCount(){
        return praiseCount;
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

}
