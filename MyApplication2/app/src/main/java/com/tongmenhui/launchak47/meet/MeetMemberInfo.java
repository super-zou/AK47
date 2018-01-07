package com.tongmenhui.launchak47.meet;

import java.util.Calendar;

/**
 * Created by haichao.zou on 2017/9/15.
 */

public class MeetMemberInfo {

    private static final String TAG = "MeetMemberInfo";

    public int cid = -1;

    //self condition
    public int uid = -1;
    private int selfSex = -1;
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
    private String selfCondition;

    //requirement
    public int ageLower;
    public int ageUpper;
    public int requirementHeight;
    public String requirementDegree;
    public String requirementLives;
    public int requirementSex = 0;
    public String illustration;
    public int self;
    public int lovedCount;
    public int loved;
    public int praised;
    public int praisedCount;
    public String pictureChain;
    public int browseCount;

    public int requirementSet = -1;

    public static final String SEX_MALE = "男生";
    public static final String SEX_FEMALE = "女生";


    /*
    public MeetMemberInfo(String realname){
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

    public int getSelfSex(){
        return selfSex;
    }
    public void setSelfSex(int selfSex){
        this.selfSex = selfSex;
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
        return degree;
    }

    public String getDegreeName(String degree){
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

    public void setJobTitle(String jobTitle){
        this.jobTitle = jobTitle;
    }
    public String getJobTitle(){
        return jobTitle;
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

    public void setBirthYear(int birthYear){
        this.birthYear = birthYear;
    }
    public int getBirthYear(){
        return birthYear;
    }

    public void setBirthMonth(int birthMonth){
        this.birthMonth = birthMonth;
    }
    public int getBirthMonth(){
        return birthMonth;
    }

    public void setBirthDay(int birthDay){
        this.birthDay = birthDay;
    }
    public int getBirthDay(){
        return birthDay;
    }

    public String getRealname(){
        return realname;
    }
    public void setRealname(String realname){
        this.realname = realname;
    }

    public void setPictureUri(String pictureUri){
        if(!"".equals(pictureUri)){
            this.pictureUri = pictureUri;
        }
    }
    public String getPictureUri(){
        return pictureUri;
    }

    public int getAge(){
        Calendar calendar= Calendar.getInstance();
        return calendar.get(calendar.YEAR) - birthYear;
    }



    public void setAgeLower(int ageLower){
        this.ageLower = ageLower;
    }
    public int getAgeLower(){
        return ageLower;
    }

    public void setAgeUpper(int ageUpper){
        this.ageUpper = ageUpper;
    }
    public int getAgeUpper(){
        return ageUpper;
    }

    public void setRequirementHeight(int requirementHeight){
        this.requirementHeight = requirementHeight;
    }
    public int getRequirementHeight(){
        return requirementHeight;
    }

    public void setRequirementDegree(String requirementDegree){
        this.requirementDegree = requirementDegree;
    }
    public String getRequirementDegree(){
        return requirementDegree;
    }

    public void setRequirementLives(String requirementLives){
        this.requirementLives = requirementLives;
    }
    public String getRequirementLives(){
        return requirementLives;
    }

    public void setRequirementSex(int requirementSex){
        this.requirementSex = requirementSex;
    }
    public String getRequirementSex(){
        if(requirementSet == 0){
            return SEX_MALE;
        }else{
            return SEX_FEMALE;
        }
    }

    public void setIllustration(String illustration){
        this.illustration = illustration;
    }
    public String getIllustration(){
        return illustration;
    }

    public void setBrowseCount(int browseCount){
        this.browseCount = browseCount;
    }
    public int getBrowseCount(){
        return browseCount;
    }

    public void setLoved(int loved){
        this.loved = loved;
    }
    public int getLoved(){
        return loved;
    }

    public void setLovedCount(int lovedCount){
        this.lovedCount = lovedCount;
    }
    public int getLovedCount(){
        return lovedCount;
    }

    public void setPraised(int praised){
        this.praised = praised;
    }
    public int getPraised(){
        return praised;
    }

    public void setPraisedCount(int praisedCount){
        this.praisedCount = praisedCount;
    }
    public int getPraisedCount(){
        return praisedCount;
    }

    public void setPictureChain(String pictureChain){
        this.pictureChain = pictureChain;
    }
    public String getPictureChain(){
        return pictureChain;
    }

    public void setRequirementSet(int requirementSet){
        this.requirementSet = requirementSet;
    }
    public int getRequirementSet(){
        return requirementSet;
    }

    public String getSelfCondition(int situation){

        selfCondition = getAge()+"岁/"+height+"CM/"+getDegreeName(getDegree());
        if(situation ==0){//for student
            selfCondition += "/"+university;
        }else{//for worker
            selfCondition += "/"+ jobTitle;
        }
        return selfCondition;
    }

    public String getRequirement(){
        String requirement = "期待遇见："+ getAgeLower()+"~"+ getAgeUpper()+"岁,"
                + getRequirementHeight()+"CM以上,"+getDegreeName(String.valueOf(getRequirementDegree()))+"以上,"
                + "住在 "+ getRequirementLives()+"的"+ getRequirementSex();

        return requirement;
    }



}
