package com.hetang.adapter;

import com.hetang.R;

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

    public static List<DataBean> getTestData() {
        List<DataBean> list = new ArrayList<>();
        list.add(new DataBean(R.drawable.image1, "相信自己,你努力的样子真的很美", 1));
        list.add(new DataBean(R.drawable.image2, "极致简约,梦幻小屋", 3));
        list.add(new DataBean(R.drawable.image3, "超级卖梦人", 3));
        list.add(new DataBean(R.drawable.image4, "夏季新搭配", 1));
        list.add(new DataBean(R.drawable.image5, "绝美风格搭配", 1));
        list.add(new DataBean(R.drawable.image6, "微微一笑 很倾城", 3));
        return list;
    }
    
    public static List<DataBean> getTestData2() {
        List<DataBean> list = new ArrayList<>();
        list.add(new DataBean(R.drawable.image7, "听风.赏雨", 3));
        list.add(new DataBean(R.drawable.image8, "迪丽热巴.迪力木拉提", 1));
        list.add(new DataBean(R.drawable.image9, "爱美.人间有之", 3));
        list.add(new DataBean(R.drawable.image10, "洋洋洋.气质篇", 1));
        list.add(new DataBean(R.drawable.image11, "生活的态度", 3));
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
