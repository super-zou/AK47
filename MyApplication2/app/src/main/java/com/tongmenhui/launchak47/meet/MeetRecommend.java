package com.tongmenhui.launchak47.meet;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetRecommend {

    public int cid = -1;

    //self condition
    public int uid = -1;
    public String realname;
    public String mPictureUri;
    public int birth_day;
    public int height;
    public String university;
    public String degree;
    public String job_title;
    public String lives;
    public int situation;
    private String self_condition;

    //requirement
    public int age_lower;
    public int age_upper;
    public int requirement_height;
    public int requirement_degree;
    public String requirement_lives;
    public int requirement_sex;
    public String illustration;
    public int self;
    public int loved_count;
    public int loved;
    public int praised;
    public int praised_count;
    public String picture_chain;

    //public int requirement_set = 0;



    public MeetRecommend(String realname){
        this.realname = realname;
    }

    public void setSelfCondition(int situation){
        self_condition = birth_day+"/"+height+"/"+degree;
        if(situation ==0){//for student
            self_condition += "/"+university+"/"+lives;
        }else{//for worker
            self_condition += "/"+job_title;
        }

    }
    public String getSelfCondition(){
        return self_condition;
    }
    public void setRealname(String realname){
        this.realname = realname;
    }
    public void setUid(int uid){
        this.uid = uid;
    }
    public void setPictureUri(String mPictureUri){
        this.mPictureUri = mPictureUri;
    }

    public int getUid(){
        return uid;
    }
    public String getPictureUri(){
        return mPictureUri;
    }

    public String getRealname(){
        return realname;
    }
}
