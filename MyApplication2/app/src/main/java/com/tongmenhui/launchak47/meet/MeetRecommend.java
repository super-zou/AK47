package com.tongmenhui.launchak47.meet;

import com.tongmenhui.launchak47.util.Slog;

import java.util.Calendar;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetRecommend {

    private static final String TAG = "MeetRecommend";

    public int cid = -1;

    //self condition
    public int uid = -1;
    public String realname;
    public String pictureUri;
    public int birth_year;
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
    public int browse_count;

    public int requirement_set = 0;

    public static final String sex_male = "男生";
    public static final String sex_female = "女生";


    /*
    public MeetRecommend(String realname){
        this.realname = realname;
    }
    */

    public int getUid(){
        return uid;
    }
    public void setUid(int uid){
        this.uid = uid;
    }

    public int getCid(){
        return cid;
    }
    public void setCid(int cid){
        this.cid = cid;
    }

    public int getHeight(){
        return height;
    }
    public void setHeight(int height){
        this.height = height;
    }
    public void setUniversity(String university){
        this.university = university;
    }
    public String getUniversity(){
        return university;
    }

    public void setDegree(String degree){
        this.degree = degree;
    }
    public String getDegree(){
        String Degree;
        switch (Integer.parseInt(degree)){
            case 0:
                Degree = "大专";
                break;
            case 1:
                Degree = "本科";
                break;
            case 2:
                Degree = "硕士";
                break;
            case 3:
                Degree = "博士";
                break;
            default:
                Degree = "硕士";
                break;
        }
        return Degree;
    }

    public void setJob_title(String job_title){
        this.job_title = job_title;
    }
    public String getJob_title(){
        return job_title;
    }

    public void setLives(String lives){
        this.lives = lives;
    }
    public String getLives(){
        return lives;
    }

    public void setSituation(int situation){
        this.situation = situation;
    }
    public int getSituation(){
        return situation;
    }

    public void setBirth_year(int birth_year){
        this.birth_year = birth_year;
    }
    public int getBirth_year(){
        return birth_year;
    }

    public String getRealname(){
        return realname;
    }
    public void setRealname(String realname){
        this.realname = realname;
    }

    public void setPictureUri(String pictureUri){
        this.pictureUri = pictureUri;
    }
    public String getPictureUri(){
        return pictureUri;
    }

    public int getAge(){
        Calendar calendar= Calendar.getInstance();
        return calendar.get(calendar.YEAR) - birth_year;
    }



    public void setAge_lower(int age_lower){
        this.age_lower = age_lower;
    }
    public int getAge_lower(){
        return age_lower;
    }

    public void setAge_upper(int age_upper){
        this.age_upper = age_upper;
    }
    public int getAge_upper(){
        return age_upper;
    }

    public void setRequirement_height(int requirement_height){
        this.requirement_height = requirement_height;
    }
    public int getRequirement_height(){
        return requirement_height;
    }

    public void setRequirement_degree(int requirement_degree){
        this.requirement_degree = requirement_degree;
    }
    public int getRequirement_degree(){
        return requirement_degree;
    }

    public void setRequirement_lives(String requirement_lives){
        this.requirement_lives = requirement_lives;
    }
    public String getRequirement_lives(){
        return requirement_lives;
    }

    public void setRequirement_sex(int requirement_sex){
        this.requirement_sex = requirement_sex;
    }
    public String getRequirement_sex(){
        if(requirement_set == 0){
            return sex_male;
        }else{
            return sex_female;
        }
    }

    public void setIllustration(String illustration){
        this.illustration = illustration;
    }
    public String getIllustration(){
        return illustration;
    }

    public void setBrowse_count(int browse_count){
        this.browse_count = browse_count;
    }
    public int getBrowse_count(){
        return browse_count;
    }

    public void setLoved(int loved){
        this.loved = loved;
    }
    public int getLoved(){
        return loved;
    }

    public void setLoved_count(int loved_count){
        this.loved_count = loved_count;
    }
    public int getLoved_count(){
        return loved_count;
    }

    public void setPraised(int praised){
        this.praised = praised;
    }
    public int getPraised(){
        return praised;
    }

    public void setPraised_count(int praised_count){
        this.praised_count = praised_count;
    }
    public int getPraised_count(){
        return praised_count;
    }

    public void setPicture_chain(String picture_chain){
        this.picture_chain = picture_chain;
    }
    public String getPicture_chain(){
        return picture_chain;
    }

    public void setRequirement_set(int requirement_set){
        this.requirement_set = requirement_set;
    }
    public int getRequirement_set(){
        return requirement_set;
    }

    public String getSelfCondition(int situation){

        self_condition = getAge()+"岁/"+height+"CM/"+getDegree();
        if(situation ==0){//for student
            self_condition += "/"+university;
        }else{//for worker
            self_condition += "/"+job_title;
        }
        self_condition += "/"+ lives;
        return self_condition;
    }

    public String getRequirement(){
        String requirement = "期待遇见："+ getAge_lower()+"~"+getAge_upper()+"岁,"
                + getRequirement_height()+"CM以上,"+getRequirement_degree()+"以上,"
                + "住在 "+getRequirement_lives()+"的"+getRequirement_sex();

        return requirement;
    }



}
