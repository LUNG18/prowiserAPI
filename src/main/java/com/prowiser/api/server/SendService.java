package com.prowiser.api.server;

import org.apache.sling.commons.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

public interface SendService {
    //获取配置文件中的账户信息
    String getSecret(String token,HttpServletRequest request);
    //推送 ufs 消息
    void sendUFSWxMsg(int num, String secret) throws Exception ;
    //推送 微信 消息
    void sendOtherWxMsg(int num,String access_token) throws Exception;
    //上传 保存数据
    String uploadFile(MultipartFile file, String sign) throws IOException, JSONException ;
    //获取当前保存的数据条数
    int getUserInfoNum();

}
