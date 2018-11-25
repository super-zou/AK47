package com.tongmenhui.launchak47.meet;

/**
 * Created by super-zou on 17-12-17.
 */

public class MeetDynamicsInteract {
    private boolean type;
    private String picture_uri;
    private String author_name;
    private int author_uid;
    private String commenter_name;
    private String commenter_uid;
    private String content;
    //private String timestamp;

    public boolean getType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public String getPictureUri() {
        return picture_uri;
    }

    public void setPictureUri(String picture_uri) {
        this.picture_uri = picture_uri;
    }

    public String getAuthorName() {
        return author_name;
    }

    public void setAuthorName(String author_name) {
        this.author_name = author_name;
    }

    public int getAuthorUid() {
        return author_uid;
    }

    public void setAuthorUid(int author_id) {
        this.author_uid = author_id;
    }

    public String getCommenterName() {
        return commenter_name;
    }

    public void setCommenterName(String commenter_name) {
        this.commenter_name = commenter_name;
    }

    public String getCommenterUid() {
        return commenter_uid;
    }

    public void setCommenterUid(String commenter_uid) {
        this.commenter_uid = commenter_uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
