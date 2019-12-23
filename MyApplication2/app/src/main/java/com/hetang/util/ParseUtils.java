package com.hetang.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.hetang.archive.ArchiveActivity;
import com.hetang.common.Dynamic;
import com.hetang.common.DynamicsInteractDetailsActivity;
import com.hetang.common.MyApplication;
import com.hetang.main.MeetArchiveActivity;
import com.hetang.meet.UserMeetInfo;
import com.hetang.meet.MeetReferenceInfo;
import com.hetang.update.UpdateCheckResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.hetang.common.DynamicsInteractDetailsActivity.DYNAMIC_COMMENT;
import static com.hetang.common.DynamicsInteractDetailsActivity.MEET_RECOMMEND_COMMENT;
import static com.hetang.meet.MeetRecommendFragment.student;

/*added by xuchunping 2018.8.2 for Json data parse*/
public class ParseUtils {
    private static final String TAG = "ParseUtils";
    private static final boolean isDebug = false;
    public static final String GET_MEET_ARCHIVE_URL = HttpUtil.DOMAIN + "?q=meet/get_archive";
    public static final String DYNAMIC_ADD = HttpUtil.DOMAIN + "?q=dynamic/action/add";
    public static final String GET_USER_PROFILE_URL = HttpUtil.DOMAIN + "?q=account_manager/get_user_profile";
    public static final String GET_SPECIFIC_EDUCATION_BACKGROUND_URL = HttpUtil.DOMAIN + "?q=personal_archive/get_specific_education_background";
    public static final String GET_SPECIFIC_WORK_EXPERIENCE_URL = HttpUtil.DOMAIN + "?q=personal_archive/get_specific_work_experience";
    public static final String GET_SPECIFIC_PRIZE_URL = HttpUtil.DOMAIN + "?q=personal_archive/get_specific_prize";
    public static final String GET_SPECIFIC_PAPER_URL = HttpUtil.DOMAIN + "?q=personal_archive/get_specific_paper";
    public static final String GET_SPECIFIC_VOLUNTEER_URL = HttpUtil.DOMAIN + "?q=personal_archive/get_specific_volunteer";
    public static final String GET_SPECIFIC_BLOG_URL = HttpUtil.DOMAIN + "?q=personal_archive/get_specific_blog";

    public static final int TYPE_EVALUATE = 0;
    public static final int TYPE_REFERENCE = 1;
    public static final int TYPE_PERSONALITY = 2;
    public static final int TYPE_HOBBY = 3;
    public static final int TYPE_CHEERING_GROUP = 4;
    public static final int TYPE_SINGLE_GROUP = 5;
    public static final int TYPE_COMMON_SEARCH = 6;

    //for dynamic value
    public static final int ADD_MEET_DYNAMIC_ACTION = 0;
    public static final int ADD_INNER_DYNAMIC_ACTION = 1;
    public static final int PRAISE_DYNAMIC_ACTION = 2;
    //for meet archive
    public static final int PRAISE_MEET_CONDITION_ACTION = 3;
    public static final int PUBLISH_MEET_CONDITION_ACTION = 4;
    public static final int EVALUATE_ACTION = 5;
    public static final int APPROVE_IMPRESSION_ACTION = 6;
    public static final int ADD_PERSONALITY_ACTION = 7;
    public static final int APPROVE_PERSONALITY_ACTION = 8;
    public static final int ADD_HOBBY_ACTION = 9;
    public static final int JOIN_CHEERING_GROUP_ACTION = 11;
    public static final int REFEREE_ACTION = 12;
    
    //for single group
    public static final int CREATE_SINGLE_GROUP_ACTION = 13;
    public static final int JOIN_SINGLE_GROUP_ACTION = 14;
    public static final int INVITE_SINGLE_GROUP_MEMBER_ACTION = 15;
    //for archive
    public static final int ADD_CHEERING_GROUP_MEMBER_ACTION = 10;
    public static final int SET_AVATAR_ACTION = 16;
    public static final int ADD_INTRODUCTION_ACTION = 17;
    public static final int ADD_EDUCATION_ACTION = 18;
    public static final int ADD_WORK_ACTION = 19;
    public static final int ADD_BLOG_ACTION = 20;
    public static final int ADD_PAPER_ACTION = 21;
    public static final int ADD_PRIZE_ACTION = 22;
    public static final int ADD_VOLUNTEER_ACTION = 23;
    public static final int ADD_SUBGROUP_ACTIVITY_ACTION = 24;
    
    //here only for notification, other notification reused with dynamic
    public static final int APPLY_CONTACTS_NF = 50;
    public static final int FOLLOWED_NF = 51;
    public static final int LOVED_NF = 52;
    public static final int DYNAMIC_PRAISED_NF = 53;
    public static final int MEET_PRAISED_NF = 54;
    public static final int COMMENT_PRAISED_NF = 55;
    public static final int REPLY_PRAISED_NF = 56;
    public static final int DYNAMIC_COMMENT_NF = 57;
    public static final int MEET_COMMENT_NF = 58;
    public static final int COMMENT_REPLY_NF = 59;
    public static final int PRAISE_MEET_COMMENT_NF = 60;
    public static final int REFEREE_INVITE_NF = 61;
    public static final int MEET_COMMENT_REPLY_NF = 62;
    public static final int APPLY_JOIN_SINGLE_GROUP_NF = 63;
    public static final int ACCEPT_CONTACTS_APPLY_NF = 64;
    
    public static List<UserMeetInfo> getBaseMeetInfoList(String responseText) {
        List<UserMeetInfo> list = null;
        Log.d(TAG, "getBaseMeetInfoList responseText:" + responseText);
        if (TextUtils.isEmpty(responseText)) {
            return null;
        }
        try {
            JSONObject response = new JSONObject(responseText);
            if (null == response) {
                return null;
            }
            
           JSONArray jsonArray = response.getJSONArray("response");
            list = getMeetInfoListFromJsonArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static List<UserMeetInfo> getMeetInfoListFromJsonArray(JSONArray jsonArray){
        List<UserMeetInfo> list = new ArrayList<>();
        if (null == jsonArray) {
            return null;
        }
        int length = jsonArray.length();
         try {
            for (int i = 0; i < length; i++) {
                UserMeetInfo userMeetInfo = new UserMeetInfo();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                setBaseProfile(userMeetInfo, jsonObject);
                list.add(userMeetInfo);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return list;
}
    
     public static List<UserProfile> getUserProfileListFromJsonArray(JSONArray jsonArray){
       List<UserProfile> list = new ArrayList<>();

       if(jsonArray != null && jsonArray.length() > 0){
           try {
               for (int i=0; i<jsonArray.length(); i++){
                   UserProfile userProfile = getUserProfileFromJSONObject(jsonArray.getJSONObject(i));
                   list.add(userProfile);
               }
           }catch (JSONException e){
               e.printStackTrace();
           }
           }

       return list;
    }
    
    public static UserProfile getUserProfile(int uid){
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        Response response = HttpUtil.sendOkHttpRequestSync(MyApplication.getContext(), GET_USER_PROFILE_URL, requestBody, null);
        if (response != null){
            try {
                String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText)) {
                    JSONObject jsonObject = new JSONObject(responseText).optJSONObject("user");
                    return ParseUtils.getUserProfileFromJSONObject(jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        return null;
    }
    
    public static List<UserMeetInfo> getRecommendMeetList(String responseText, boolean isUpdate) {
        List<UserMeetInfo> list = null;
        if (isDebug) Log.d(TAG, "getRecommendMeetList responseText:" + responseText);
        if (TextUtils.isEmpty(responseText)) {
            return null;
        }
         try {
            JSONObject recommend_response = new JSONObject(responseText);
            if (null == recommend_response) {
                return null;
            }
            if (isUpdate == true){
                int current = recommend_response.optInt("current");
                if (isDebug) Slog.d(TAG, "----------------->current: "+current);
                SharedPreferencesUtils.setRecommendLast(MyApplication.getContext(), String.valueOf(current));
            }
              JSONArray recommendation = recommend_response.optJSONArray("recommendation");
            if (null == recommendation && recommendation.length() == 0) {
                return null;
            }
            list = new ArrayList<UserMeetInfo>();
            int length = recommendation.length();
            UserMeetInfo userMeetInfo = null;
            for (int i = 0; i < length; i++) {
                JSONObject recommender = recommendation.getJSONObject(i);
                userMeetInfo = setMeetMemberInfo(recommender);
                list.add(userMeetInfo);
            }
             } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static UserMeetInfo setBaseProfile(UserMeetInfo userMeetInfo, JSONObject member){
        userMeetInfo.setName(member.optString("name"));
        userMeetInfo.setRealname(member.optString("realname"));
        userMeetInfo.setUid(member.optInt("uid"));
        userMeetInfo.setSex(member.optInt("sex"));
        userMeetInfo.setInit(member.optString("init"));
        if (!member.optString("avatar").equals("null")){
            userMeetInfo.setAvatar(member.optString("avatar"));
        }
        userMeetInfo.setBirthYear(member.optInt("birth_year"));
        userMeetInfo.setHeight(member.optInt("height"));


        userMeetInfo.setDegree(member.optString("degree"));
        userMeetInfo.setUniversity(member.optString("university"));
        userMeetInfo.setConstellation(member.optString("constellation"));
        userMeetInfo.setHometown(member.optString("hometown"));
        userMeetInfo.setNation(member.optString("nation"));
        userMeetInfo.setReligion(member.optString("religion"));

        userMeetInfo.setSituation(member.optInt("situation"));
        userMeetInfo.setCid(member.optInt("cid"));
        
        if (member.optInt("situation") == student){
            userMeetInfo.setMajor(member.optString("major"));
        }else {
            userMeetInfo.setPosition(member.optString("position"));
            userMeetInfo.setIndustry(member.optString("industry"));
        }
        
        if(member.optString("living") != null && !TextUtils.isEmpty(member.optString("living"))){
            userMeetInfo.setLiving(member.optString("living"));
        }

        return userMeetInfo;
    }
    
    public static UserMeetInfo setMeetMemberInfo(JSONObject member) {
        UserMeetInfo userMeetInfo = new UserMeetInfo();

        setBaseProfile(userMeetInfo, member);
        //requirement
        userMeetInfo.setAgeLower(member.optInt("age_lower"));
        userMeetInfo.setAgeUpper(member.optInt("age_upper"));
        userMeetInfo.setRequirementHeight(member.optInt("requirement_height"));
        userMeetInfo.setRequirementDegree(member.optString("requirement_degree"));
        userMeetInfo.setRequirementLiving(member.optString("requirement_living"));
        userMeetInfo.setRequirementSex(member.optInt("requirement_sex"));
        userMeetInfo.setIllustration(member.optString("illustration"));
        
        userMeetInfo.setVisitCount(member.optInt("visit_count"));
        userMeetInfo.setLovedCount(member.optInt("loved_count"));
        userMeetInfo.setPraisedCount(member.optInt("praised_count"));
        userMeetInfo.setLoved(member.optInt("loved"));
        userMeetInfo.setPraised(member.optInt("praised"));
        userMeetInfo.setCommentCount(member.optInt("comment_count"));

        userMeetInfo.setActivityCount(member.optInt("activity_count"));
        JSONArray pictureArray = member.optJSONArray("photo");
        
        if(pictureArray != null && pictureArray.length() > 0){
            if(isDebug) Slog.d(TAG, "---------------->pictureArray length: "+pictureArray.length());
            userMeetInfo.setPictureCount(pictureArray.length());
            try {
                JSONObject thumbnail = (JSONObject) pictureArray.get(0);
                userMeetInfo.setThumbnail(thumbnail.optString("uri")+thumbnail.optString("filename"));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        
        JSONObject refereeObj = member.optJSONObject("reference");
        if (refereeObj != null){
            userMeetInfo.setRefereeName(refereeObj.optString("account"));
            userMeetInfo.setRefereeAvatar(refereeObj.optString("avatar"));
            userMeetInfo.setReferenceContent(refereeObj.optString("content"));
        }

        return userMeetInfo;
    }
    
    public static List<UserMeetInfo> getMeetDiscoveryList(String responseText) {
        List<UserMeetInfo> list = null;
        if(isDebug) Log.d(TAG, "getMeetDiscoveryList responseText:" + responseText);
        if (TextUtils.isEmpty(responseText)) {
            return null;
        }
        try {
            JSONObject recommend_response = new JSONObject(responseText);
            if (null == recommend_response) {
                return null;
            }
            
            JSONArray discovery = recommend_response.getJSONArray("discovery");
            if (null == discovery) {
                return null;
            }
            list = new ArrayList<UserMeetInfo>();
            int length = discovery.length();
            UserMeetInfo userMeetInfo = null;
            for (int i = 0; i < length; i++) {
                JSONObject discoveryObj = discovery.getJSONObject(i);
                userMeetInfo = setMeetMemberInfo(discoveryObj);
                
                list.add(userMeetInfo);
            }
        } catch (JSONException e) {

        }
        return list;
    }
    
    public static List<MeetReferenceInfo> getMeetReferenceList(String responseText) {
        List<MeetReferenceInfo> meetReferenceInfoList = new ArrayList<MeetReferenceInfo>();
        if (!TextUtils.isEmpty(responseText)) {
            try {
                JSONObject referenceObj = new JSONObject(responseText);
                if (referenceObj != null) {
                    JSONArray referenceArray = referenceObj.optJSONArray("reference");
                    MeetReferenceInfo meetReferenceInfo = null;
                    if (referenceArray != null && referenceArray.length() > 0) {
                        for (int i = 0; i < referenceArray.length(); i++) {
                            meetReferenceInfo = new MeetReferenceInfo();
                            JSONObject reference = referenceArray.getJSONObject(i);
                            meetReferenceInfo.setRefereeName(reference.optString("name"));
                            meetReferenceInfo.setRelation(reference.optString("relation"));
                            String profile = "";
                            if (reference.optInt("situation") == 0) {
                                String degree = meetReferenceInfo.getDegreeName(reference.optString("degree"));
                                profile = reference.optString("university") + "·"
                                                    + degree + "·" + reference.optString("major");
                            } else {
                                profile = reference.optString("position") + "·" + reference.optString("industry");
                            }
                            meetReferenceInfo.setRefereeProfile(profile);
                            meetReferenceInfo.setReferenceContent(reference.optString("content"));
                            meetReferenceInfo.setCreated(reference.getInt("created"));
                            meetReferenceInfo.setAvatar(reference.optString("avatar"));
                            meetReferenceInfo.setUid(reference.optInt("uid"));

                            meetReferenceInfoList.add(meetReferenceInfo);
                        }
                        
                         }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return meetReferenceInfoList;
    }
    
     public static void startMeetArchiveActivity(final Context context, final int uid, UserMeetInfo userMeetInfo) {
        Intent intent = new Intent(context, MeetArchiveActivity.class);
        if (userMeetInfo != null){
            intent.putExtra("meet", userMeetInfo);
        }
         
         intent.putExtra("uid", uid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    public static void startMeetArchiveActivity(final Context context, final int uid){
        startMeetArchiveActivity(context, uid, null);
    }
    
    public static void startArchiveActivity(final Context context, final int uid){
        Intent intent = new Intent(context, ArchiveActivity.class);
        intent.putExtra("uid", uid);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    public static void startMeetConditionDetails(final Context context, final int uid, final int cid, UserMeetInfo userMeetInfo){
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        if (userMeetInfo != null){
            intent.putExtra("meetRecommend", userMeetInfo);
        }
        intent.putExtra("type", MEET_RECOMMEND_COMMENT);
        intent.putExtra("uid", uid);
        intent.putExtra("cid", cid);
        context.startActivity(intent);
    }
    
    public static void startDynamicDetails(final Context context,final long did, Dynamic dynamic) {
        Intent intent = new Intent(MyApplication.getContext(), DynamicsInteractDetailsActivity.class);
        intent.putExtra("type", DYNAMIC_COMMENT);
        if (dynamic != null){
            intent.putExtra("dynamic", dynamic);
        }
        intent.putExtra("did", did);
        context.startActivity(intent);
    }
    
     public static UserProfile getUserProfileFromJSONObject(JSONObject userObject){
        UserProfile userProfile = new UserProfile();
        if (userObject == null){
            return null;
        }
        userProfile.setUid(userObject.optInt("uid"));
        userProfile.setName(userObject.optString("name"));
        userProfile.setPass(userObject.optString("pass"));
        userProfile.setCreated(userObject.optInt("created"));
        userProfile.setAccess(userObject.optInt("access"));
        userProfile.setLogin(userObject.optInt("login"));
        userProfile.setAuthorSelf(userObject.optBoolean("author"));
        userProfile.setInit(userObject.optString("init"));
         
         //for ext data
        userProfile.setAvatar(userObject.optString("avatar"));
        userProfile.setSex(userObject.optInt("sex"));
        userProfile.setSituation(userObject.optInt("situation"));
        userProfile.setRealname(userObject.optString("realname"));
        userProfile.setLiving(userObject.optString("living"));
        userProfile.setHometown(userObject.optString("hometown"));
         
        userProfile.setMajor(userObject.optString("major"));
        userProfile.setDegree(userObject.optString("degree"));
        userProfile.setUniversity(userObject.optString("university"));
        userProfile.setPosition(userObject.optString("position"));
        userProfile.setIndustry(userObject.optString("industry"));
        userProfile.setSummary(userObject.optString("summary"));
        userProfile.setIntroduction(userObject.optString("introduction"));
        userProfile.setCid(userObject.optInt("cid"));

        return userProfile;
    }
    
    public static UpdateCheckResult fromJson(String jsonStr){
        UpdateCheckResult updateCheckResult = new UpdateCheckResult();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONObject versionInfo = jsonObject.optJSONObject("version");
            boolean hasUpdate = jsonObject.optBoolean("hasUpdate");
            if (jsonObject != null){
                updateCheckResult.hasUpdate = hasUpdate;
                if (hasUpdate == true){
                    int mode = versionInfo.optInt("mode");
                    if (mode == 1){
                        updateCheckResult.isIgnorable = false;
                    }
                    updateCheckResult.versionName = versionInfo.optString("version_name");
                    updateCheckResult.versionCode = versionInfo.optInt("version_code");
                    updateCheckResult.updateLog = versionInfo.optString("content");
                    updateCheckResult.apkUrl = HttpUtil.DOMAIN + versionInfo.optString("download_url");
                    updateCheckResult.apkSize = versionInfo.optInt("size");
                    
                    }

                return updateCheckResult;
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

        return null;
    }

}

