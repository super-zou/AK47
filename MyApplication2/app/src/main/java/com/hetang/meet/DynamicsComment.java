package com.hetang.meet;

/**
 * Created by haichao.zou on 2017/12/19.
 */

public class DynamicsComment {
    private int mType;
    private int mAid;
    private int mCid;
    private String mPictureUrl;
    private String mAuthorName;
    private Long mAuthorUid;
    private String mCommenterName;
    private Long mCommenterUid;
    private String mContent;
    private int mTimeStamp;

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getAid() {
        return mAid;
    }

    public void setAid(int aid) {
        mAid = aid;
    }

    public int getCid() {
        return mCid;
    }

    public void setCid(int cid) {
        mCid = cid;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        mPictureUrl = pictureUrl;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public void setAuthorName(String authorName) {
        mAuthorName = authorName;
    }

    public Long getAuthorUid() {
        return mAuthorUid;
    }

    public void setAuthorUid(Long authorUid) {
        mAuthorUid = authorUid;
    }

    public String getCommenterName() {
        return mCommenterName;
    }

    public void setCommenterName(String commenterName) {
        mCommenterName = commenterName;
    }

    public Long getCommenterUid() {
        return mCommenterUid;
    }

    public void setCommenterUid(Long commenterUid) {
        mCommenterUid = commenterUid;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public int getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        mTimeStamp = timeStamp;
    }

}
