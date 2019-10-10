package com.hetang.util;

import android.text.TextUtils;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private static final String TAG = "UserProfile";
    public static final String SEX_MALE = "男生";
    public static final String SEX_FEMALE = "女生";
    
    public int uid = -1;
    public String name = "";
    public String realname = "";
    public int sex = 0;
    public String avatar = "";
    public String living = "";
    public int situation = 0;
    public String degree ;
    public String major;
    public String university = "";
    public String title = "";
    public String industry;
    private String summary = "";
    private String introduction = "";
    private int created;
    private int access;
    private int login;
    private int pictureFid;
    private String init;
    private int cid = 0;//this is {meet_condition} table's cid, who has add meet info
    
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getInit() { return init; }

    public void setInit(String init) {
        this.init = init;
    }
    
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public int getCreated() { return created; }
    public void setCreated(int created){ this.created = created; }
    
    public int getAccess() { return access; }
    public void setAccess(int access) { this.access = access; }

    public int getLogin() { return  login; }
    public void setLogin(int login) { this.login = login; }

    public int getPictureFid() { return pictureFid; }
    public void setPictureFid(int pictureFid) { this.pictureFid = pictureFid; }
    
    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
    
    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        if (degree != null && !TextUtils.isEmpty(degree)) {
            this.degree = degree;
        }
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLiving() {
        return living;
    }

    public String getIndustry() {return industry; }
    
     public void setIndustry(String industry) { this.industry = industry; }

    public void setLiving(String living) {
        this.living = living;
    }

    public int getSituation() {
        return situation;
    }

    public void setSituation(int situation) {
        this.situation = situation;
    }
    
    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        if (avatar != null && !TextUtils.isEmpty(avatar)) {
            this.avatar = avatar;
        }
    }
    
    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }


    public String getSummary() {return  summary;}
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIntroduction() {return  introduction;}
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

}
