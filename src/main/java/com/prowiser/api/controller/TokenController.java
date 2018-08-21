package com.prowiser.api.controller;

import com.prowiser.api.server.impl.SendServiceImpl;
import com.prowiser.api.utils.MD5Utils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("account")
public class TokenController {
    private static Logger log = LoggerFactory.getLogger(TokenController.class);

    @Value("${bak_url}")
    private String BAK_URL;

    @RequestMapping(value = "add",method = RequestMethod.GET)
    public String initToken(String key,String appid,String secret){
        String token = MD5Utils.getMD5Token(appid+secret);
        if(!SendServiceImpl.tokens.contains(token)) {
            SendServiceImpl.tokens.add(token);
            FileWriter writer = null;
            try {
                File file = new File(this.getClass().getClassLoader().getResource("").getPath(), "group/" + token.substring(0, 10) + ".properties");
                if (!file.exists())
                    file.createNewFile();
                writer = new FileWriter(file, true);
                writer.write("ACCOUNT=" + key.toUpperCase());
                writer.write(System.getProperty("line.separator"));
                writer.write("APP_ID=" + appid);
                writer.write(System.getProperty("line.separator"));
                writer.write("ST_SECRET=" + secret);
                writer.flush();

                file = new File(this.getClass().getClassLoader().getResource("").getPath(), "group/app.properties");
                writer = new FileWriter(file, true);
                writer.write(System.getProperty("line.separator"));
                writer.write(token);
            } catch (IOException e) {
                log.info("write account error", e);
                return "error";
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
            log.info("add a new account:"+key.toUpperCase());
        }
        return token;
    }

    @RequestMapping("backups")
    public String backups() throws IOException {
        //删除
        File dir = new File(BAK_URL);
        if(!dir.exists())
            dir.mkdir();
        for(File f : dir.listFiles()){
            f.delete();
        }
        //复制
        File temp = new File(this.getClass().getClassLoader().getResource("").getPath(), "group/");//此group为项目中的group目录
        for(File old : temp.listFiles()){
            File file = new File(BAK_URL+old.getName());
            Files.copy(old.toPath(),file.toPath());
        }
        return "success";
    }
}
