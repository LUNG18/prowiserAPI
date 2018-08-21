package com.prowiser.api.controller;

import com.prowiser.api.server.SignService;
import com.prowiser.api.utils.MD5Utils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("sign")
public class SignController {

    private static Logger log = LoggerFactory.getLogger(SignController.class);
    @Resource
    private SignService signService;


    @RequestMapping("get")
    public String getSign(String appid, String secret){
        if(StringUtils.isBlank(appid) || StringUtils.isBlank(secret))
            return "{\"code\":500, \"msg\":\"appid or secret is empty\"}";

        String token = MD5Utils.getMD5Token(appid+secret);
        File file = new File(this.getClass().getClassLoader().getResource("").getPath(), "group/app.properties");
        List<String> tokens = new ArrayList<>();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String str = null;
            while((str = br.readLine()) != null) {
                tokens.add(str);
            }
        } catch (Exception e) {
        }
        if(!tokens.contains(token))
            return "{\"code\":500, \"msg\":\"appid or secret is wrong\"}";

        String sign = MD5Utils.getMD5String(appid+new SimpleDateFormat("HHmmssSSS").format(new Date())+secret);
        if(signService.getSignBySign(sign) != null){
            log.info("sign is exist,and get it again");
            this.getSign(appid,secret);
        }
        return "{\"code\":200, \"sign\":\""+sign+"\"}";
    }
}
