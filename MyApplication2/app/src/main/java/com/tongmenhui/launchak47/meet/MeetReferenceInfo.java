package com.tongmenhui.launchak47.meet;

/**
 * Created by super-zou on 18-9-22.
 */

public class MeetReferenceInfo {
    private String refereeName;
    private String refereeProfile;
    private String content;
    private Long created;
    private String headUri;

    public String getRefereeName(){
        return refereeName;
    }
    public void setRefereeName(String referee_name){
        this.refereeName = referee_name;
    }

    public String getRefereeProfile(){
        return refereeProfile;
    }
    public void setRefereeProfile(String referee_profile){
        this.refereeProfile = referee_profile;
    }
    public String getReferenceContent(){
        return content;
    }
    public void setReferenceContent(String reference_content){
        this.content = reference_content;
    }

    public Long getCreated(){
        return created;
    }

    public void setCreated(Long created){
        this.created = created;
    }

    public String getHeadUri(){
        return headUri;
    }
    public void setHeadUri(String headUri){
        this.headUri = headUri;
    }

}
