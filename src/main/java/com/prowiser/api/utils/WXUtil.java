package com.prowiser.api.utils;

import com.prowiser.api.pojo.AccessToken;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class WXUtil {
    private static Logger log = LoggerFactory.getLogger(WXUtil.class);
    private static Map<String,AccessToken> redis = new HashMap<>();

    public static AccessToken getWXToken(String appId,String appSecret) throws Exception {
        AccessToken access_token = redis.get(appId);
        if(access_token!=null && access_token.getPastAt()<new Date().getTime()/1000)//校验是否过期
            access_token = null;
        if(access_token == null || access_token.getAccessToken().equals("")){
            log.info("get token from weixin");
            String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+ appId+"&secret="+ appSecret;
            HttpClientCall httpClientCall = new HttpClientCall();
            String ret = httpClientCall.callHttps(tokenUrl,"");
            JSONObject jsonObject = new JSONObject(ret);
            if (null != jsonObject) {
                try {
                    access_token = new AccessToken();
                    access_token.setAccessToken(jsonObject.getString("access_token"));
                    access_token.setExpiresin(jsonObject.getInt("expires_in"));
                    access_token.setPastAt(new Date().getTime()/1000 + 7100);//过期时间
                } catch (JSONException e) {
                    access_token = null;
                    log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
                }
            }
            redis.put(appId, access_token);
        }
        return access_token;
    }



    /*public static void main(String[] args) {
        try {
            String token = getWXToken("wx3bd94678ca39cec2","8feed99c5aa40d10b18258e88570a2a3").getAccessToken();
            String tokenUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
            String tempId = "o-qaB1WvbeMkdmbwB1cBfZoA-Xg2aa4pqTAIrvk9LK8";
            JSONObject objR = new JSONObject();
            objR.put("touser", "omkIGswYQ3AOispKpYQzLXo3Puvk");
            objR.put("template_id", tempId);
            objR.put("url", "http://www.baidu.com");
            JSONObject dataJson = new JSONObject();
            dataJson.put("first",new JSONObject().put("value","1").put("color","red"));
            dataJson.put("keyword1",new JSONObject().put("value","2").put("color","red"));
            dataJson.put("keyword2",new JSONObject().put("value","3").put("color","red"));
            dataJson.put("keyword3",new JSONObject().put("value","4").put("color","red"));
            dataJson.put("remark",new JSONObject().put("value","5").put("color","red"));
            objR.put("data", dataJson);
            HttpClientCall httpClientCall = new HttpClientCall();
            String ret = httpClientCall.callHttps(tokenUrl+token,objR.toString(),null,5000,5000);
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
