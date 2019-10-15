package com.hetang.util;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;

public class CommonPickerView {
    public ArrayList<CommonBean> getOptionsMainItem(Context context, String jsonFile){
        String jsonData = Utility.getJson(context,jsonFile);//获取assets目录下的json文件数据

        ArrayList<CommonBean> jsonBean = parseData(jsonData);

        return jsonBean;
    }
    
    public ArrayList<ArrayList<String>> getOptionsSubItems(ArrayList<CommonBean> jsonBean){
        ArrayList<ArrayList<String>> optionsItems = new ArrayList<>();
        for (int i=0;i<jsonBean.size();i++){//遍历mainItem
            ArrayList<String> subItemList = new ArrayList<>();//该的mainItem的子列表（第二级）

            for (int c=0; c<jsonBean.get(i).getSubItemList().size(); c++){//遍历mainItem所有subItemList
                String subItem = jsonBean.get(i).getSubItemList().get(c);
                subItemList.add(subItem);//添加sub item
            }

            /**
             * 添加sub数据
             */
            optionsItems.add(subItemList);
        }

        return optionsItems;
    }
    
    public ArrayList<CommonBean> parseData(String result) {//Gson 解析
        ArrayList<CommonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                CommonBean entity = gson.fromJson(data.optJSONObject(i).toString(), CommonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
    }
}
