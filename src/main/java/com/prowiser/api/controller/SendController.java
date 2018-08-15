package com.prowiser.api.controller;

import com.prowiser.api.pojo.AccessToken;
import com.prowiser.api.server.impl.SendServiceImpl;
import com.prowiser.api.utils.WXUtil;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("send")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SendController {
    private static Logger log = LoggerFactory.getLogger(SendController.class);
    @Resource
    private SendServiceImpl sendServiceImpl;

    @RequestMapping(value = "msg",method = RequestMethod.POST)
    public String sendWxMsg(@RequestParam("file")MultipartFile file, String token, HttpServletRequest request,String sign) throws JSONException {
        JSONObject ret = new JSONObject();
        ret.put("code",200);
        ret.put("msg","start to push");
        if(file.isEmpty()) {//空文件判断
            ret.put("code",404);
            ret.put("msg", "file not received !");
            return ret.toString();
        }
        if(!file.getOriginalFilename().endsWith("json")) {//文件类型判断
            ret.put("code",501);
            ret.put("msg", "file must be end with '.json'!");
            return ret.toString();
        }
        String secret = sendServiceImpl.getSecret(token,request);//识别token 并确定secret
        if(secret == null) {
            ret.put("code", 500);
            ret.put("msg", "token is wrong !");
            return ret.toString();
        }
        int num = 0;
        try {
            num = sendServiceImpl.uploadFile(file,sign);//文件上传 数据保存
            log.info("uploadFile num = "+num);
        }catch (JSONException e) {
            log.info("uploadFile",e);
            ret.put("code",500);
            ret.put("msg","parameter format error");
        } catch (Exception e) {
            log.info("unknown error",e);
            ret.put("code",500);
            ret.put("msg","unknown error !");
        }

        if(num != 0){
            int users = sendServiceImpl.getUserInfoNum(sign);
            log.info("json user number = "+num+", push user number = "+users);
            if(users == num){
                new Thread(){//另起线程发送
                    @Override
                    public void run() {
                        if(sendServiceImpl.UFSlist.contains(token)){//ufs的请求直接调用接口
                            for(int i=0; i<3; i++){//发送失败 重试3次
                                try {
                                    sendServiceImpl.sendUFSWxMsg(i,secret);
                                } catch (Exception e) {
                                    log.info("send ufs Msg",e);
                                }
                            }
                        }else{//其他请求走微信原生接口
                            try {
                                HttpSession session = request.getSession();
                                if(session!=null){
                                    String appId = (String) session.getAttribute("appid");
                                    if(appId!=null){
                                        AccessToken access_token = WXUtil.getWXToken(appId,secret);
                                        session.setAttribute("accessToken",access_token);
                                        for(int i=0; i<3; i++) {//发送失败 重试3次
                                            sendServiceImpl.sendOtherWxMsg(i, access_token.getAccessToken());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.info("send other Msg",e);
                            }
                        }
                    }
                }.start();
            }
        }
        return ret.toString();
    }


}
