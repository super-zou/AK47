package com.tongmenhui.launchak47.util;

import android.text.TextUtils;
import android.util.Log;

import com.tongmenhui.launchak47.meet.MeetMemberInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*added by xuchunping 2018.8.2 for Json data parse*/
public class ParseUtils {
    private static final String TAG = "ParseUtils";
    public static List<MeetMemberInfo> getMeetList(String responseText){
        List<MeetMemberInfo> list = null;
        Log.d(TAG, "getMeetList responseText:"+responseText);
        if (TextUtils.isEmpty(responseText)){
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
            for (int i=0; i< length; i++){
                JSONObject recommender = recommendation.getJSONObject(i);
                meetMemberInfo = new MeetMemberInfo();

                meetMemberInfo.setRealname(recommender.getString("realname"));
                meetMemberInfo.setUid(recommender.getInt("uid"));
                meetMemberInfo.setPictureUri(recommender.getString("picture_uri"));
                meetMemberInfo.setBirthYear(recommender.getInt("birth_year"));
                meetMemberInfo.setHeight(recommender.getInt("height"));
                meetMemberInfo.setUniversity(recommender.getString("university"));

                meetMemberInfo.setDegree(recommender.getString("degree"));
                meetMemberInfo.setJobTitle(recommender.getString("job_title"));
                meetMemberInfo.setLives(recommender.getString("lives"));
                meetMemberInfo.setSituation(recommender.getInt("situation"));

                //requirement
                meetMemberInfo.setAgeLower(recommender.getInt("age_lower"));
                meetMemberInfo.setAgeUpper(recommender.getInt("age_upper"));
                meetMemberInfo.setRequirementHeight(recommender.getInt("requirement_height"));
                meetMemberInfo.setRequirementDegree(recommender.getString("requirement_degree"));
                meetMemberInfo.setRequirementLives(recommender.getString("requirement_lives"));
                meetMemberInfo.setRequirementSex(recommender.getInt("requirement_sex"));
                meetMemberInfo.setIllustration(recommender.getString("illustration"));


                // meetMemberInfo.setSelf(recommender.getInt("self"));
                meetMemberInfo.setBrowseCount(recommender.getInt("browse_count"));
                meetMemberInfo.setLovedCount(recommender.getInt("loved_count"));
                // meetMemberInfo.setLoved(recommender.getInt("loved"));
                // meetMemberInfo.setPraised(recommender.getInt("praised"));
                meetMemberInfo.setPraisedCount(recommender.getInt("praised_count"));
                //  meetMemberInfo.setPictureChain(recommender.getString("pictureChain"));
                // meetMemberInfo.setRequirementSet(recommender.getInt("requirementSet"));


                list.add(meetMemberInfo);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return list;
    }

}
