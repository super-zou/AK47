package com.hetang.meet;

import android.text.TextUtils;

import com.hetang.util.UserProfile;

import java.util.Calendar;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class UserMeetInfo extends UserProfile {

    private static final String TAG = "UserMeetInfo";
    public static final String SEX_MALE = "男生";
    public static final String SEX_FEMALE = "女生";
    
    //self condition
    public int birthYear = 0;
    public int birthMonth = 0;
    public int birthDay = 0;
    public int height = 0;

    public String constellation = "";
    public String nation = "";
    public String religion = "";
    public String thumbnail = "";

    //requirement
    public int ageLower = 0;
    public int ageUpper = 0;
    public int requirementHeight = 0;
    public String requirementDegree = "";
    public String requirementLiving = "";
    public int requirementSex = 0;
    public String illustration = "";
    
    public int lovedCount = 0;
    public int loved = 0;
    public int praised = 0;
    public int praisedCount = 0;
    public int pictureCount = 0;
    public int activityCount = 0;
    public int visitCount = 0;
    public int commentCount = 0;
    public int requirementSet = 0;
    private String refereeName = "";
    private String refereeAvatar = "";
    private String referenceContent = "";

    private int selfSex = -1;
    private String selfCondition = "";

    private String profile = "";
    
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
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
    
    public String getConstellation(){ return constellation; }
    public void  setConstellation(String constellation) {this.constellation = constellation; }

    public String getNation() { return nation; }
    public void setNation(String nation) { this.nation = nation; }

    public String getReligion() { return  religion; }
    public void setReligion(String religion) { this.religion = religion; }

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

    public String getRequirementLiving() {
        return requirementLiving;
    }

    public void setRequirementLiving(String requirementLiving) {
        this.requirementLiving = requirementLiving;
    }
    
    public String getRequirementSex() {
        if (requirementSex == 0) {
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
        if(illustration != "null"){
            this.illustration = illustration;
        }
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
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

    public int getPictureCount() { return  pictureCount; }
    public void setPictureCount( int pictureCount ) { this.pictureCount = pictureCount; }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
    public int getActivityCount() { return  activityCount; }
    public void setActivityCount(int activityCount) { this.activityCount = activityCount; }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getRequirementSet() {
        return requirementSet;
    }

    public void setRequirementSet(int requirementSet) {
        this.requirementSet = requirementSet;
    }
    
    public String getSelfCondition(int situation) {
        selfCondition = "";
        if (getAge() != 0) {
            selfCondition = getAge() + "岁 | ";
        }
        if (height != 0) {
            selfCondition += height + "cm | ";
        }

        if (!TextUtils.isEmpty(getConstellation())) {
            selfCondition += getConstellation();
        }

        /*
        if (situation != 0) {//for work
            if (!"".equals(getPosition())) {
                selfCondition += " | "+getPosition();
            }
        }
        */

        return selfCondition;
    }
    
    public String getRequirement() {

        String requirement = "期待遇见: ";
        if (getAgeLower() != 0 && getAgeUpper() != 0) {
            requirement += getAgeLower() + "~" + getAgeUpper() + "岁，";
        } else {// meet requirement not set, should return ""
            return "";
        }

        if (getRequirementHeight() != 0) {
            requirement += getRequirementHeight() + "cm以上，";
        }

        if (!"".equals(getDegreeName(String.valueOf(getRequirementDegree())))) {
            requirement += getDegreeName(String.valueOf(getRequirementDegree()))+"，";
        }
        
        if (!"".equals(getRequirementLiving())) {
            requirement += "住在" + getRequirementLiving();
        }

        if (!"".equals(getRequirementSex())) {
            requirement += "的" + getRequirementSex();
        }

        return requirement;
    }

    public String getRefereeName() { return refereeName; }
    public void setRefereeName(String refereeName){
        this.refereeName = refereeName;
    }

    public String getRefereeAvatar() { return refereeAvatar; }
    public void setRefereeAvatar(String refereeAvatar){ this.refereeAvatar = refereeAvatar; }

    public String getReferenceContent(){ return referenceContent; }
    public void setReferenceContent(String referenceContent) { this.referenceContent = referenceContent; }

}

