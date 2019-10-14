package com.hetang.meet;

import com.hetang.util.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haichao.zou on 2017/12/19.
 */
public class DynamicsComment extends UserProfile {

    private long mDid;
    private long mCommentId;
    private long mRid;
    private String mAuthorName;
    private int mAuthorUid;
    private String mReplierName;
    private int mReplierUid;
    private int mReplierSex;
    private String mContent;
    private String mReplyContent;
    private int mCreated;
    private int praiseCount;
    private int replyCount;
    private int replyPraiseCount;

    public List<DynamicsComment> replyList = new ArrayList<>();
    
    public long getDid() {
        return mDid;
    }

    public void setDid(long did) {
        mDid = did;
    }

    public long getCommentId() {
        return mCommentId;
    }

    public void setCommentId(long commentId) {
        mCommentId = commentId;
    }

    public long getRid() { return mRid; }

    public void setRid(long rid) { mRid = rid; }
    
    public String getAuthorName() {
        return mAuthorName;
    }

    public void setAuthorName(String authorName) {
        mAuthorName = authorName;
    }

    public int getAuthorUid() {
        return mAuthorUid;
    }

    public void setAuthorUid(int authorUid) {
        mAuthorUid = authorUid;
    }

    public String getReplierName() {
        return mReplierName;
    }
    
    public void setReplierName(String replierName) {
        mReplierName = replierName;
    }

    public int getReplierUid() {
        return mReplierUid;
    }

    public void setReplierUid(int replierUid) {
        mReplierUid = replierUid;
    }

    public int getReplierSex(){
        return mReplierSex;
    }

    public void setReplierSex(int replierSex){
        mReplierSex = replierSex;
    }
    
    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getReplyContent(){
        return mReplyContent;
    }

    public void setReplyContent(String replyContent){
        mReplyContent = replyContent;
    }

    public int getCreated() {
        return mCreated;
    }
    
    public void setCreated(int created) {
        mCreated = created;
    }

    public int getPraiseCount() { return praiseCount; }
    public void setPraiseCount(int praiseCount) { this.praiseCount = praiseCount; }

    public int getReplyCount() { return  replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public int getReplyPraiseCount() { return  replyPraiseCount; }
    public void setReplyPraiseCount(int replyPraiseCount) { this.replyPraiseCount = replyPraiseCount; }

}
