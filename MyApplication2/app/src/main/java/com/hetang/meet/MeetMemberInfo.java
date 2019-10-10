package com.hetang.meet;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetMemberInfo implements Serializable {

    public static final String SEX_MALE = "男生";
    public static final String SEX_FEMALE = "女生";
    private static final String TAG = "MeetMemberInfo";
    public int cid = -1;
    //self condition
    public int uid = -1;
    public String realname = "";
    public String pictureUri = "";
    public int birthYear = 0;
    public int birthMonth = 0;
    public int birthDay = 0;
    public int height = 0;
    public String university = "";
    public String degree = "-1";
    public String jobTitle = "";
    public String lives = "";
    public int situation = 0;
    //requirement
    public int ageLower = 0;
    public int ageUpper = 0;
    public int requirementHeight = 0;
    public String requirementDegree = "";
    public String requirementLives = "";
    public int requirementSex = 0;
    public String illustration = "";
    public int self;
    public int lovedCount = 0;
    public int loved = 0;
    public int praised = 0;
    public int praisedCount = 0;
    public String pictureChain = "";
    public int browseCount = 0;
    public int requirementSet = 0;
    private int sex = 0;
    private int selfSex = -1;
    private String selfCondition = "";
    private String major;
    private String company;
    private String profile = "";


    /*
    public MeetMemberInfo(String realname){
        this.realname = realname;
    }
    */

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

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
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
        if (degree != null && !TextUtils.isEmpty(degree)) {
            this.degree = degree;
        }
    }

    public String getDegreeName(String degree) {
        String Degree = "";
        if (degree != "" && !degree.equals("null")) {
            switch (Integer.parseInt(degree)) {
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
                    Degree = "不限";
                    break;
            }
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
        if (birthYear != 0) {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(calendar.YEAR) - birthYear;
        }

        return 0;
    }

    public int getAgeLower() {
        return ageLower;
    }

    public void setAgeLower(int ageLower) {
        this.ageLower = ageLower;
    }

    public int getAgeUpper() {
        return ageUpper;
    }

    public void setAgeUpper(int ageUpper) {
        this.ageUpper = ageUpper;
    }

    public int getRequirementHeight() {
        return requirementHeight;
    }

    public void setRequirementHeight(int requirementHeight) {
        this.requirementHeight = requirementHeight;
    }

    public String getRequirementDegree() {
        return requirementDegree;
    }

    public void setRequirementDegree(String requirementDegree) {
        this.requirementDegree = requirementDegree;
    }

    public String getRequirementLives() {
        return requirementLives;
    }

    public void setRequirementLives(String requirementLives) {
        this.requirementLives = requirementLives;
    }

    public String getRequirementSex() {
        if (requirementSet == 0) {
            return SEX_MALE;
        } else {
            return SEX_FEMALE;
        }
    }

    public void setRequirementSex(int requirementSex) {
        this.requirementSex = requirementSex;
    }

    public String getIllustration() {
        return illustration;
    }

    public void setIllustration(String illustration) {
        this.illustration = illustration;
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

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getRequirementSet() {
        return requirementSet;
    }

    public void setRequirementSet(int requirementSet) {
        this.requirementSet = requirementSet;
    }

    public String getProfile() {
        String profile;
        if (getSituation() == 0) {//student
            profile = getUniversity() + "·" + getMajor() + "·" + getDegree();
        } else {
            profile = getJobTitle() + "·" + getCompany() + "·" + getLives();
        }
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getSelfCondition(int situation) {
        selfCondition = "";
        if (getAge() != 0) {
            selfCondition = getAge() + "岁/";
        }
        if (height != 0) {
            selfCondition += height + "CM/";
        }
        if (!"".equals(getDegreeName(getDegree()))) {
            selfCondition += getDegreeName(getDegree());
        }
        //selfCondition = getAge()+"岁/"+height+"CM/"+getDegreeName(getDegree());
        if (situation == 0) {//for student
            if (!"".equals(university)) {
                selfCondition += university;
            }

        } else {//for worker
            if (!"".equals(jobTitle)) {
                selfCondition += jobTitle;
            }
        }
        return selfCondition;
    }

    public String getRequirement() {

        String requirement = "期待遇见";
        if (getAgeLower() != 0 && getAgeUpper() != 0) {
            requirement += getAgeLower() + "~" + getAgeUpper() + "岁,";
        } else {// meet requirement not set, should return ""
            return "";
        }

        if (getRequirementHeight() != 0) {
            requirement += getRequirementHeight() + "CM以上,";
        }

        if (!"".equals(getDegreeName(String.valueOf(getRequirementDegree())))) {
            requirement += getDegreeName(String.valueOf(getRequirementDegree())) + "以上,";
        }

        if (!"".equals(getRequirementLives())) {
            requirement += "住在 " + getRequirementLives();
        }

        if (!"".equals(getRequirementSex())) {
            requirement += "的" + getRequirementSex();
        }
        /*
        String requirement = "期待遇见："+ getAgeLower()+"~"+ getAgeUpper()+"岁,"
                + getRequirementHeight()+"CM以上,"+getDegreeName(String.valueOf(getRequirementDegree()))+"以上,"
                + "住在 "+ getRequirementLives()+"的"+ getRequirementSex();
        */

        return requirement;
    }

}
