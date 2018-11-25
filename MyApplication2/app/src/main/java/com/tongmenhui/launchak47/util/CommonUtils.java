package com.tongmenhui.launchak47.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*added by xuchunping 2018.7.21 增加公共方法*/
public class CommonUtils {

    /**
     * 验证邮箱输入是否合法
     *
     * @param strEmail
     * @return
     */
    public static boolean isEmail(String strEmail) {
        // String strPattern =
        // "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        String mailPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        Pattern p = Pattern.compile(mailPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    /**
     * 验证是否是手机号码
     *
     * @param strPhone
     * @return
     */
    public static boolean isMobile(String strPhone) {
        String mobilePattern = "^((13[0-9])|(15[^4,\\D])|(17[0,7])|(18[0-9]))\\d{8}$";
        Pattern pattern = Pattern.compile(mobilePattern);
        Matcher matcher = pattern.matcher(strPhone);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }
}
