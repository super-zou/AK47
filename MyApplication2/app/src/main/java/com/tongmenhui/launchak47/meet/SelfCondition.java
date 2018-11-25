package com.tongmenhui.launchak47.meet;

import java.util.Calendar;

/**
 * Created by super-zou on 18-1-13.
 */

public class SelfCondition {
    public static final String SEX_MALE = "男生";
    public static final String SEX_FEMALE = "女生";
    private static final String TAG = "SelfCondition";
    public int cid = -1;
    //self condition
    public int uid = -1;
    public String realname;
    public String pictureUri = "";
    public int birthYear;
    public int birthMonth;
    public int birthDay;
    public int height;
    public String university;
    public String degree;
    public String jobTitle;
    public String lives;
    public int situation;
    public int self;
    public int lovedCount;
    public int loved;
    public int praised;
    public int praisedCount;
    public String pictureChain;
    public int browseCount;

    public int requirementSet = -1;
    private int selfSex = -1;
    private String selfCondition;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getSelfSex() {
        return selfSex;
    }

    public void setSelfSex(int selfSex) {
        this.selfSex = selfSex;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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
        this.degree = degree;
    }

    public int getDegreeIndex() {
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

    public String getDegreeName(int degree) {
        String Degree;
        switch (degree) {
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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getLives() {
        return lives;
    }

    public void setLives(String lives) {
        this.lives = lives;
    }

    public int getSituation() {
        return situation;
    }

    public void setSituation(int situation) {
        this.situation = situation;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public int getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(int birthMonth) {
        this.birthMonth = birthMonth;
    }

    public int getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(int birthDay) {
        this.birthDay = birthDay;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getPictureUri() {
        return pictureUri;
    }

    public void setPictureUri(String pictureUri) {
        if (!"".equals(pictureUri)) {
            this.pictureUri = pictureUri;
        }
    }

    public int getAge() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(calendar.YEAR) - birthYear;
    }

    public int getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(int browseCount) {
        this.browseCount = browseCount;
    }

    public int getLoved() {
        return loved;
    }

    public void setLoved(int loved) {
        this.loved = loved;
    }

    public int getLovedCount() {
        return lovedCount;
    }

    public void setLovedCount(int lovedCount) {
        this.lovedCount = lovedCount;
    }

    public int getPraised() {
        return praised;
    }

    public void setPraised(int praised) {
        this.praised = praised;
    }

    public int getPraisedCount() {
        return praisedCount;
    }

    public void setPraisedCount(int praisedCount) {
        this.praisedCount = praisedCount;
    }

    public String getPictureChain() {
        return pictureChain;
    }

    public void setPictureChain(String pictureChain) {
        this.pictureChain = pictureChain;
    }

    public int getRequirementSet() {
        return requirementSet;
    }

    public void setRequirementSet(int requirementSet) {
        this.requirementSet = requirementSet;
    }

    public String getSelfCondition(int situation) {

        selfCondition = getAge() + "岁/" + height + "CM/" + getDegree();
        if (situation == 0) {//for student
            selfCondition += "/" + university;
        } else {//for worker
            selfCondition += "/" + jobTitle;
        }
        return selfCondition;
    }

}
