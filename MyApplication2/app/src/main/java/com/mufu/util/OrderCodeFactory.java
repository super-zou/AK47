package com.mufu.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 订单编码生成器，生成12位数字编码，
 * @生成规则 1位商品类型+1位支付类别+4位时间戳+6位(随机数)
 */

public class OrderCodeFactory {

    private static int mGoodsType = GoodsType.EXPERIENCE.ordinal();
    private static int mPayType = PayType.WECHAT.ordinal();
    
    /**
     * 货品类别
     */
    public enum GoodsType {
        EXPERIENCE, GUIDE
    }
    /**
     * 支付类别
     */
    public enum PayType {
        WECHAT, ALIPAY, BANK
    }
    /**
     * 随即编码
     */
    private static final int[] r = new int[]{7, 9, 6, 2, 8, 1, 3, 0, 5, 4};
    
    /**
     * 用户id和随机数总长度
     */
    private static final int maxLength = 6;

    /**
     * 设置货品类别
     */
    public static void setGoodsType(GoodsType goodsType){
        mGoodsType = goodsType.ordinal();
    }

    /**
     * 设置货品类别
     */
    public static void setPayType(PayType payType){
        mPayType = payType.ordinal();
    }
    
    /**
     * 生成4位时间戳
     */
    private static String getDateTime() {
        DateFormat sdf = new SimpleDateFormat("MMdd");
        return sdf.format(new Date());
    }

    /**
     * 生成固定长度随机码
     *
     * @param n 长度
     */
    private static long getRandom(long n) {
        long min = 1, max = 9;
        for (int i = 1; i < n; i++) {
            min *= 10;
            max *= 10;
        }
        long rangeLong = (((long) (new Random().nextDouble() * (max - min)))) + min;
        return rangeLong;
    }
    
    /**
     * 生成不带类别标头的编码
     */
    private static synchronized String getCode() {
        return getDateTime() + getRandom(maxLength);
    }

    /**
     * 生成订单单号编码
     */
    public static String getOrderCode() {
        return String.valueOf(mGoodsType)+String.valueOf(mPayType)+getCode();
    }
}
