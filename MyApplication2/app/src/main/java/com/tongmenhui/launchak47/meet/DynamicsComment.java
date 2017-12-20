package com.tongmenhui.launchak47.meet;

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

    public void setType(int type){
        mType = type;
    }
    public int getType(){
        return mType;
    }
    public void setAid(int aid){
        mAid = aid;
    }
    public int getAid(){
        return mAid;
    }
    public void setCid(int cid){
        mCid = cid;
    }
    public int getCid(){
        return mCid;
    }
    public void setPictureUrl(String pictureUrl){
        mPictureUrl = pictureUrl;
    }
    public String getPictureUrl(){
        return mPictureUrl;
    }
    public void setAuthorName(String authorName){
        mAuthorName = authorName;
    }
    public String getAuthorName(){
        return mAuthorName;
    }
    public void setAuthorUid(Long authorUid){
        mAuthorUid = authorUid;
    }
    public Long getAuthorUid(){
        return mAuthorUid;
    }
    public void setCommenterName(String commenterName){
        mCommenterName = commenterName;
    }
    public String getCommenterName(){
        return mCommenterName;
    }
    public void setCommenterUid(Long commenterUid){
        mCommenterUid = commenterUid;
    }
    public Long getCommenterUid(){
        return mCommenterUid;
    }

    public void setContent(String content){
        mContent = content;
    }
    public String getContent(){
        return mContent;
    }

}
