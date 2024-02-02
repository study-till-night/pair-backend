package com.shuking.pairBackend.utils;

import java.util.regex.Pattern;

public class RegexUtils {

    /**
     * 检验字符串是否不包含非法字符
     * @param str 待检测字符串
     * @return false 不符合 true 符合
     */
    public static boolean checkValidString(String str){
        return Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(str).find();
    }
}
