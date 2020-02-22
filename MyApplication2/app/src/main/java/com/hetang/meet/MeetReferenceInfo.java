package com.hetang.meet;
import com.hetang.util.UserProfile;

/**
 * Created by super-zou on 18-9-22.
 */

public class MeetReferenceInfo extends UserProfile{
    private String refereeName;
    private String relation;
    private String refereeProfile;
    private String content;
    private Long created;
    private String avatar;
    private int uid = 0;
    private int rid;

    public void setRid(int rid){ this.rid = rid; }
    public int getRid(){ return rid; }

    public String getRefereeName() {
        return refereeName;
    }

    public void setRefereeName(String referee_name) {
        this.refereeName = referee_name;
    }
    
    public String getRelation(){
        return relation;
    }

    public void setRelation(String relation){
        this.relation = relation;
    }

    public String getRefereeProfile() {
        return refereeProfile;
    }

    public void setRefereeProfile(String referee_profile) {
        this.refereeProfile = referee_profile;
    }

    public String getReferenceContent() {
        return content;
    }

    public void setReferenceContent(String reference_content) {
        this.content = reference_content;
    }

    public String getContent() { return content; }
}
