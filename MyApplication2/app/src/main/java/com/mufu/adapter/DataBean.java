package com.mufu.adapter;

import com.mufu.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataBean {
    public Integer imageRes;
    public String imageUrl;
    public String title;
    public int viewType;
    private List<DataBean> dataBeanList = new ArrayList<>();

    public DataBean(){

    }
    
    public DataBean(Integer imageRes, String title, int viewType) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
    }

    public DataBean(String imageUrl, String title, int viewType) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.viewType = viewType;
    }

    public void setBannerData(String url){
        dataBeanList.add(new DataBean(url, "", 1));
    }
    
     public List<DataBean> getBannerData(){
        return dataBeanList.size()>0?dataBeanList:null;
    }
    
   public List<DataBean> getRecommendBannerData() {
        List<DataBean> list = new ArrayList<>();
        list.add(new DataBean(R.drawable.banner1, "1", 1));
        list.add(new DataBean(R.drawable.banner2, "2", 1));
        list.add(new DataBean(R.drawable.banner3, "3", 1));
        list.add(new DataBean(R.drawable.banner4, "4", 1));
        list.add(new DataBean(R.drawable.banner5, "5", 1));
        list.add(new DataBean(R.drawable.banner6, "6", 1));
        return list;
    }

    public static List<String> getColors(int size) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            list.add(getRandColor());
        }
        return list;
    }
    
    public static String getRandColor() {
        String R, G, B;
        Random random = new Random();
        R = Integer.toHexString(random.nextInt(256)).toUpperCase();
        G = Integer.toHexString(random.nextInt(256)).toUpperCase();
        B = Integer.toHexString(random.nextInt(256)).toUpperCase();

        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        return "#" + R + G + B;
    }
}
