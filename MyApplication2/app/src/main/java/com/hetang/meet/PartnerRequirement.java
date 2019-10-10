package com.hetang.meet;

/**
 * Created by super-zou on 18-1-13.
 */

public class PartnerRequirement {
    public static final String SEX_MALE = "男生";
    public static final String SEX_FEMALE = "女生";
    private static final String TAG = "PartnerRequirement";
    //requirement
    public int ageLower;
    public int ageUpper;
    public int requirementHeight;
    public String requirementDegree;
    public String requirementLives;
    public int requirementSex = 0;
    public String illustration;

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

    public int getDegreeIndex() {
        if (requirementDegree.equals("大专")) {
            return 0;
        }
        if (requirementDegree.equals("本科")) {
            return 1;
        }
        if (requirementDegree.equals("硕士")) {
            return 2;
        }
        if (requirementDegree.equals("博士")) {
            return 3;
        }
        return 4;
    }

    public String getDegreeName(String degree) {
        String Degree;
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
                Degree = "硕士";
                break;
        }
        return Degree;
    }

    public String getRequirementLives() {
        return requirementLives;
    }

    public void setRequirementLives(String requirementLives) {
        this.requirementLives = requirementLives;
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
        this.illustration = illustration;
    }

    public String getRequirement() {
        String requirement = "期待遇见：" + getAgeLower() + "~" + getAgeUpper() + "岁,"
                + getRequirementHeight() + "CM以上," + getDegreeName(String.valueOf(getRequirementDegree())) + "以上,"
                + "住在 " + getRequirementLives() + "的" + getRequirementSex();

        return requirement;
    }
}
