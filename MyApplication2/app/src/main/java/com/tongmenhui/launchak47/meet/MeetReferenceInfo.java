package com.tongmenhui.launchak47.meet;

/**
 * Created by super-zou on 18-9-22.
 */

public class MeetReferenceInfo {
    private String referee_name;
    private String referee_profile;
    private String reference_content;

    public String getRefereeName(){
        return referee_name;
    }
    public void setRefereeName(String referee_name){
        this.referee_name = referee_name;
    }

    public String getRefereeProfile(){
        return referee_profile;
    }
    public void setRefereeProfile(String referee_profile){
        this.referee_profile = referee_profile;
    }
    public String getReferenceContent(){
        return reference_content;
    }
    public void setReferenceContent(String reference_content){
        this.reference_content = reference_content;
    }
}
