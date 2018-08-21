package com.prowiser.api.utils;

import java.security.MessageDigest;

/**
 * Created by ＰＲＯＷＩＳＥＲ on 2016/8/17.
 */
public class MD5Utils {

    public static String getMD5String(String str){
        try {
            MessageDigest mdInst = MessageDigest.getInstance("md5");
            StringBuffer hexValue = new StringBuffer();
            mdInst.update(str.getBytes());
            byte[] b = mdInst.digest();
            int i;
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(i));
            }
            return hexValue.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getMD5Token(String ss){
        String s = ss==null?"":ss;
        char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        try {
            byte[] strTemp = s.getBytes();
            MessageDigest md5Temp = MessageDigest.getInstance("md5");
            md5Temp.update(strTemp);
            byte[] md =md5Temp.digest();
            int j = md.length;
            char[] str = new char[j*2];
            int k = 0;
            for(int i=0;i<j;i++){
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0>>>4&0xf];
                str[k++] = hexDigits[byte0&0xf];
            }
            return new String(str);
        }catch (Exception e){

        }
        return null;
    }
}
