package com.tongmenhui.launchak47.meet;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetRecommend {

    public int cid = -1;

    //self condition
    public int uid = -1;
    public String realname;
    public String picture_uri;
    public int birth_day;
    public int height;
    public String university;
    public String degree;
    public String job_title;
    public String lives;
    public int situation;

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



    public MeetRecommend(){

    }

    public void setSelfCondition(){

    }

    public String getRealnameName(){
        return realname;
    }
}
