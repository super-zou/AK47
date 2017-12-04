package com.tongmenhui.launchak47.meet;

/**
 * Created by haichao.zou on 2017/11/20.
 */

public class MeetDynamics extends MeetMemberInfo {

    private long aid;
    private long created;
    private String content;
    private String activity_picture;

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
    public long getCreated(){
        return created;
    }
    public void setCreated(long created){
        this.created = created;
    }
    public String getActivityPicture(){
        return activity_picture;
    }
    public void setActivityPicture(String activity_picture){
        this.activity_picture = activity_picture;
    }
}
