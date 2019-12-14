package com.hetang.util;

import android.text.TextUtils;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private static final String TAG = "UserProfile";
    //public static final String SEX_MALE = "男生";
    //public static final String SEX_FEMALE = "女生";
    public boolean authorSelf = false;
    public int uid = -1;
    public String pass;
    public String name = "";
    public String realname = "";
    public int sex = 0;
    public String avatar = "";
    public String living = "";
    public String hometown = "";
    public int situation = -1;//default -1 is not set; 0 is student; 1 is work
    public String degree;
    public String major;
    public String university = "";
    public String position = "";
    public String industry;
    private String summary = "";
    private String introduction = "";
    private String profile = "";
    private int created;
    private int access;
    private int login;
    private boolean isLeader = false;

    private String init;
    private int cid = 0;//this is {meet_condition} table's cid, who has add meet info

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public void setAuthorSelf(boolean authorSelf) {
        this.authorSelf = authorSelf;
    }

    public boolean getAuthorSelf() {
        return authorSelf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public int getLogin() {
        return login;
    }

    public void setLogin(int login) {
        this.login = login;
    }

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
        if (university != null && !"null".equals(university)) {
            this.university = university;
        }
    }

    public String getDegree() {
        return degree;
    }

    public int getDegreeIndex() {
        if (degree.equals("其它")) {
            return -1;
        }
        if (degree.equals("大专")) {
            return 0;
        }
        if (degree.equals("本科")) {
            return 1;
        }
        if (degree.equals("硕士")) {
            return 2;
        }
        if (degree.equals("博士")) {
            return 3;
        }
        return 4;
    }

    public String getDegreeName(String degree) {
        String Degree = "";
        if (degree != null && !TextUtils.isEmpty(degree) && !degree.equals("null")) {
            switch (Integer.parseInt(degree)) {
                case -1:
                    Degree = "不限";
                    break;
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
        }
        return Degree;
    }

    public void setDegree(String degree) {
        if (degree != null && !"null".equals(degree)) {
            this.degree = degree;
        }
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        if (position != null && !"null".equals(position)) {
            this.position = position;
        }
    }

    public String getLiving() {
        return living;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        if (hometown != null && !"null".equals(hometown)) {
            this.hometown = hometown;
        }
    }


    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        if (industry != null && !"null".equals(industry)) {
            this.industry = industry;
        }
    }

    public void setLiving(String living) {
        if (living != null && !"null".equals(living)) {
            this.living = living;
        }
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
        if (realname != null && !"null".equals(realname)) {
            this.realname = realname;
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        if (avatar != null && !TextUtils.isEmpty(avatar) && !"null".equals(avatar)) {
            this.avatar = avatar;
        }
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        if (major != null && !"null".equals(major)) {
            this.major = major;
        }
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        if (summary != null && !"null".equals(summary)) {
            this.summary = summary;
        }
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        if (introduction != null && !"null".equals(introduction)) {
            this.introduction = introduction;
        }
    }

    public String getBaseProfile() {
        String profile = "";
        if (getSituation() != -1) {
            if (getSituation() == 0) {//student
                profile = getUniversity() + "·" + getDegreeName(getDegree());
                if (!TextUtils.isEmpty(getMajor())) {
                    profile += "·" + getMajor();
                }
            } else {
                profile = getPosition();
                if (!TextUtils.isEmpty(getIndustry())) {
                    profile += "·" + getIndustry();
                }
                if (!TextUtils.isEmpty(getLiving())) {
                    profile += "·" + getLiving();
                }
            }
        }

        return profile;
    }

    public void setProfile(String profile) {
        if (profile != null && !"null".equals(profile)) {
            this.profile = profile;
        }
    }

    public String getProfile() {
        return profile;
    }

    public void setLeader(boolean isLeader){
        this.isLeader = isLeader;
    }

    public boolean getLeader(){
        return isLeader;
    }

}
