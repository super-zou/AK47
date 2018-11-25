package com.tongmenhui.launchak47.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tongmenhui.launchak47.meet.ArchivesActivity;
import com.tongmenhui.launchak47.meet.MeetMemberInfo;
import com.tongmenhui.launchak47.meet.MeetReferenceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/*added by xuchunping 2018.8.2 for Json data parse*/
public class ParseUtils {

    private static final String TAG = "ParseUtils";
    private static final String GET_MEET_ARCHIVE_URL = HttpUtil.DOMAIN + "?q=meet/get_archive";

    public static List<MeetMemberInfo> getMeetList(String responseText) {
        List<MeetMemberInfo> list = null;
        Log.d(TAG, "getMeetList responseText:" + responseText);
        if (TextUtils.isEmpty(responseText)) {
            return null;
        }
        try {
            JSONObject recommend_response = new JSONObject(responseText);
            if (null == recommend_response) {
                return null;
            }
            JSONArray recommendation = recommend_response.getJSONArray("recommendation");
            if (null == recommendation) {
                return null;
            }
            list = new ArrayList<MeetMemberInfo>();
            int length = recommendation.length();
            MeetMemberInfo meetMemberInfo = null;
            for (int i = 0; i < length; i++) {
                JSONObject recommender = recommendation.getJSONObject(i);
                meetMemberInfo = setMeetMemberInfo(recommender);
                list.add(meetMemberInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static MeetMemberInfo setMeetMemberInfo(JSONObject member) {
        MeetMemberInfo meetMemberInfo = new MeetMemberInfo();

        meetMemberInfo.setRealname(member.optString("realname"));
        meetMemberInfo.setUid(member.optInt("uid"));
        meetMemberInfo.setSex(member.optInt("sex"));
        meetMemberInfo.setPictureUri(member.optString("picture_uri"));
        meetMemberInfo.setBirthYear(member.optInt("birth_year"));
        meetMemberInfo.setHeight(member.optInt("height"));
        meetMemberInfo.setUniversity(member.optString("university"));

        meetMemberInfo.setDegree(member.optString("degree"));
        meetMemberInfo.setJobTitle(member.optString("job_title"));
        meetMemberInfo.setLives(member.optString("lives"));
        meetMemberInfo.setSituation(member.optInt("situation"));

        //requirement
        meetMemberInfo.setAgeLower(member.optInt("age_lower"));
        meetMemberInfo.setAgeUpper(member.optInt("age_upper"));
        meetMemberInfo.setRequirementHeight(member.optInt("requirement_height"));
        meetMemberInfo.setRequirementDegree(member.optString("requirement_degree"));
        meetMemberInfo.setRequirementLives(member.optString("requirement_lives"));
        meetMemberInfo.setRequirementSex(member.optInt("requirement_sex"));
        meetMemberInfo.setIllustration(member.optString("illustration"));

        // meetMemberInfo.setSelf(recommender.optInt("self"));
        meetMemberInfo.setBrowseCount(member.optInt("browse_count"));
        meetMemberInfo.setLovedCount(member.optInt("loved_count"));
        // meetMemberInfo.setLoved(recommender.optInt("loved"));
        // meetMemberInfo.setPraised(recommender.optInt("praised"));
        meetMemberInfo.setPraisedCount(member.optInt("praised_count"));
        //  meetMemberInfo.setPictureChain(recommender.optString("pictureChain"));
        // meetMemberInfo.setRequirementSet(recommender.optInt("requirementSet"));

        return meetMemberInfo;
    }

    public static List<MeetMemberInfo> getMeetDiscoveryList(String responseText) {
        List<MeetMemberInfo> list = null;
        Log.d(TAG, "getMeetDiscoveryList responseText:" + responseText);
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
            list = new ArrayList<MeetMemberInfo>();
            int length = discovery.length();
            MeetMemberInfo meetMemberInfo = null;
            for (int i = 0; i < length; i++) {
                JSONObject discoveryObj = discovery.getJSONObject(i);
                meetMemberInfo = setMeetMemberInfo(discoveryObj);
                ;

                list.add(meetMemberInfo);
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
                            meetReferenceInfo.setRefereeName(reference.optString("realname"));
                            String profile = "";
                            if (reference.optInt("situation") == 0) {
                                profile = reference.optString("university") + "."
                                        + reference.optString("degree") + "."
                                        + reference.optString("major");
                            } else {
                                profile = reference.optString("job_title") + "." + reference.optString("company");
                            }
                            meetReferenceInfo.setRefereeProfile(profile);
                            meetReferenceInfo.setReferenceContent(reference.optString("content"));
                            meetReferenceInfo.setCreated(reference.getLong("created"));
                            meetReferenceInfo.setHeadUri(reference.optString("picture_uri"));

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

    public static void getMeetArchive(final Context context, int uid) {
        RequestBody requestBody = new FormBody.Builder().add("uid", String.valueOf(uid)).build();
        HttpUtil.sendOkHttpRequest(context, GET_MEET_ARCHIVE_URL, requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() != null) {
                    String responseText = response.body().string();
                    Slog.d(TAG, "==========get archive response text : " + responseText);
                    if (responseText != null) {
                        if (!TextUtils.isEmpty(responseText)) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseText).optJSONObject("archive");
                                MeetMemberInfo meetMemberInfo = setMeetMemberInfo(jsonObject);
                                Intent intent = new Intent(context, ArchivesActivity.class);
                                // Log.d(TAG, "meet:"+meet+" uid:"+meet.getUid());
                                intent.putExtra("meet", meetMemberInfo);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                context.startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
}
