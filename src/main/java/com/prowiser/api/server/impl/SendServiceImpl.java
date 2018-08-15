package com.prowiser.api.server.impl;

import com.prowiser.api.mapper.SendMapper;
import com.prowiser.api.pojo.PushDetail;
import com.prowiser.api.pojo.SignDetail;
import com.prowiser.api.server.SendService;
import com.prowiser.api.utils.HttpClientCall;
import org.apache.commons.io.FileUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class SendServiceImpl implements SendService {
    private static Logger log = LoggerFactory.getLogger(SendServiceImpl.class);
    private static int  stSourceId = 56;
    private static String accessToken;
    private static String stSecret;
    private static String stUrl = "https://api.unileverfoodsolutions.com.cn/app-client/clientapi/messageTemplate";
    private static String stWXUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    private static int socialTouchConcurrentNum = 5;//5个线程推送
    private static CountDownLatch latch = null;
    public static List<String> tokens = new ArrayList<>();
    private static Integer SID = 0;
    private int NUM = 0;


    @Resource
    private SendMapper sendMapper;

    @PostConstruct
    public void appInit(){
        if(tokens.size()==0){
            try {
                FileReader fr = new FileReader(this.getClass().getClassLoader().getResource("").getPath()+"/group/app.properties");
                BufferedReader bf = new BufferedReader(fr);
                String str;
                // 按行读取字符串
                while ((str = bf.readLine()) != null) {
                    tokens.add(str);
                }
                bf.close();
                fr.close();
            } catch (Exception e) {
                log.info("appInit",e);
            }
        }
        log.info("tokens size is "+tokens.size());
    }

    @Override
    public String getSecret(String token,HttpServletRequest request){
        String secret = null;
        try {
            String propName = "xxx";
            String _token = (String) request.getSession().getAttribute("token");
            if(_token!=null && _token.equals(token)){
                request.getSession().removeAttribute("token");
                secret = (String) request.getSession().getAttribute("secret");
            }else {
                request.getSession().removeAttribute("appid");
                request.getSession().removeAttribute("secret");
                request.getSession().setAttribute("token",token);
                if(tokens.contains(token)){
                    propName = token.substring(0,10);//配置文件截取10位
                }else {
                    log.info("token is not exist");
                    throw new Exception("token is wrong");
                }

                String propPath = "group/"+propName+".properties";
                InputStream in = SendServiceImpl.class.getClassLoader().getResourceAsStream(propPath);
                Properties prop = new Properties();
                prop.load(in);
                secret = prop.getProperty("ST_SECRET");
                String appId = prop.getProperty("APP_ID",null);
                request.getSession().setAttribute("appid",appId);
                request.getSession().setAttribute("secret",secret);
            }
        } catch (Exception e) {
            log.info("getSecret",e);
        }
        return secret;
    }

    @Override
    public void sendUFSWxMsg(int num, String secret) throws Exception {
        stSecret = secret;
        List<PushDetail> pushDetailList = sendMapper.selectPushDetailByRetryAndStatus(num,5000);// 按照重试次数查询
        if(pushDetailList.size() != 0){
            List<PushDetail>[] listArr = splitList(pushDetailList);
            latch = new CountDownLatch(socialTouchConcurrentNum);
            for (int i = 0; i < socialTouchConcurrentNum; i++) {
                final List<PushDetail> subList = listArr[i];
                final int taskNo = i;
                if(subList!=null) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                doPushUFS(num, subList);
                            } catch (Exception e) {
                                log.info("doPushUFS",e);
                            }
                            latch.countDown();
                            log.info("Task " + num+"_"+taskNo + " finished!");
                        }
                    }.start();
                }else{
                    latch.countDown();
                }
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.info("CountDownLatch",e);
            }
            this.sendUFSWxMsg(num, secret);//递归调用，
        }
    }

    @Override
    public void sendOtherWxMsg(int num,String access_token) throws Exception {
        accessToken = access_token;
        List<PushDetail> pushDetailList = sendMapper.selectPushDetailByRetryAndStatus(num,5000);// 按照重试次数查询
        if(pushDetailList.size() != 0){
            List<PushDetail>[] listArr = splitList(pushDetailList);
            latch = new CountDownLatch(socialTouchConcurrentNum);
            for (int i = 0; i < socialTouchConcurrentNum; i++) {
                final List<PushDetail> subList = listArr[i];
                final int taskNo = i;
                if(subList!=null) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                doPushWX(num, subList);
                            } catch (Exception e) {
                                log.info("doPushWX",e);
                            }
                            latch.countDown();
                            log.info("Task " + num+"_"+taskNo + " finished!");
                        }
                    }.start();
                }else{
                    latch.countDown();
                }
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.info("CountDownLatch",e);
            }
            this.sendUFSWxMsg(num, access_token);//递归调用，
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)//事务 -- 检查json文件的同时插入数据  如果出错 全部回滚
    public String uploadFile(MultipartFile file, String sign) throws IOException, JSONException {
        //保存上传文件
        File upload = new File(this.getClass().getClassLoader().getResource("").getPath(),"upload/"+getDateStr()+"/");
        if(!upload.exists())
            upload.mkdirs();
        String uploadPath = upload.getPath() + File.separator + file.getOriginalFilename();
        file.transferTo(new File(uploadPath));
        log.info("json file upload===" + uploadPath);
        //保存数据到数据库
        SignDetail signDetail = new SignDetail(null,sign, (int) (new Date().getTime()/1000));
        sendMapper.insertSendSign(signDetail);
        SID = signDetail.getId();
        log.info("sign id = "+SID);
        String json = FileUtils.readFileToString(new File(uploadPath), "UTF-8");
        JSONArray data = new JSONArray(json);
        int num = 0;//统计写入数据库的数量
        for(int i=0; i<data.length(); i++){
            JSONObject obj = data.getJSONObject(i);
            //校验json文件是否符合所需格式
            if(!obj.has("tempId") || !obj.has("content"))
                throw new JSONException("json file format(1) error !");
            JSONArray users = obj.getJSONArray("content");
            String tempId = obj.getString("tempId");
            for(int j=0; j<users.length(); j++){
                JSONObject userInfo = users.getJSONObject(j);
                if(!userInfo.has("openid") || !userInfo.has("text") || !userInfo.has("link"))
                    throw new JSONException("json file format(2) error !");
                JSONObject text = userInfo.getJSONObject("text");
                if(!text.has("first") || !text.has("keyword1"))
                    throw new JSONException("json file format(3) error !");
                Iterator<String> it = text.keys();
                while (it.hasNext()){
                    String key = it.next();
                    JSONObject _obj = text.getJSONObject(key);
                    if(!_obj.has("value"))
                        throw new JSONException("json file format(4) error !");
                }
                sendMapper.insertSendInfo(tempId,userInfo.toString(),SID,new Date().getTime()/1000);
                num ++;
            }
        }
        NUM = num;
        return uploadPath;
    }

    @Override
    public int getUserInfoNum() {
        return sendMapper.selectPushInfoNumByStatus(0, SID);
    }



    private void doPushUFS(int num, List<PushDetail> pushDetailList) throws Exception {
        for(int i=0; i<pushDetailList.size(); i++){
            PushDetail pushDetail = pushDetailList.get(i);
            JSONObject objR = new JSONObject();
            objR.put("template_id", pushDetail.getTempId());
            JSONObject userInfo = new JSONObject(pushDetail.getUserInfo());
            objR.put("touser", userInfo.getString("openid"));
            objR.put("url", userInfo.getString("link"));
            JSONObject text =userInfo.getJSONObject("text");
            Iterator<String> it = text.keys();
            JSONObject dataJson = new JSONObject();
            while(it.hasNext()){
                String key = it.next();
                JSONObject obj = text.getJSONObject(key);
                JSONObject msg = new JSONObject();
                msg.put("value",obj.getString("value"));
                if(obj.has("color"))
                    msg.put("color", obj.getString("color"));
                dataJson.put(key,msg);
            }
            objR.put("data", dataJson);
            String entity = "source=" + stSourceId + "&secret=" + stSecret + "&data=" + URLEncoder.encode(objR.toString(),"utf8");
            HttpClientCall http = new HttpClientCall();
            String retsult = http.callHttps(stUrl, entity,"application/x-www-form-urlencoded", 5000, 5000);
            if (retsult != null) {
                JSONObject obj = new JSONObject(retsult);
                if ("0".equals(obj.getString("code"))&&"success".equalsIgnoreCase(obj.getString("message"))) {
                    //标记该用户发送成功
                    sendMapper.updatePushInfoStatusById(pushDetail.getId(),new Date().getTime()/1000,2);
                } else {
                    //发送失败，抛异常，准备重新发送
                    updatePushInfo(num, pushDetail);
                }
            }else{
                //发送失败，抛异常，准备重新发送
                updatePushInfo(num, pushDetail);
            }
        }
    }

    private void doPushWX(int num, List<PushDetail> pushDetailList) throws Exception {
        for(int i=0; i<pushDetailList.size(); i++){
            PushDetail pushDetail = pushDetailList.get(i);
            JSONObject objR = new JSONObject();
            objR.put("template_id", pushDetail.getTempId());
            JSONObject userInfo = new JSONObject(pushDetail.getUserInfo());
            objR.put("touser", userInfo.getString("openid"));
            objR.put("url", userInfo.getString("link"));
            JSONObject text =userInfo.getJSONObject("text");
            Iterator<String> it = text.keys();
            JSONObject dataJson = new JSONObject();
            while(it.hasNext()){
                String key = it.next();
                JSONObject obj = text.getJSONObject(key);
                JSONObject msg = new JSONObject();
                msg.put("value",obj.getString("value"));
                if(obj.has("color"))
                    msg.put("color", obj.getString("color"));
                dataJson.put(key,msg);
            }
            objR.put("data", dataJson);
            HttpClientCall http = new HttpClientCall();
            String retsult = http.callHttps(stWXUrl+accessToken, objR.toString(),"application/x-www-form-urlencoded", 5000, 5000);
            if (retsult != null) {
                JSONObject obj = new JSONObject(retsult);
                if ("0".equals(obj.getString("errcode"))&&"ok".equalsIgnoreCase(obj.getString("errmsg"))) {
                    //标记该用户发送成功
                    sendMapper.updatePushInfoStatusById(pushDetail.getId(),new Date().getTime()/1000,2);
                } else {
                    //发送失败，抛异常，准备重新发送
                    updatePushInfo(num, pushDetail);
                }
            }else{
                //发送失败，抛异常，准备重新发送
                updatePushInfo(num, pushDetail);
            }
        }
    }

    private List[] splitList(List<PushDetail> list){
        int len = list.size()/socialTouchConcurrentNum;
        List[] lists = new List[socialTouchConcurrentNum];
        if(list.size()>=socialTouchConcurrentNum) {
            for (int i = 0; i < socialTouchConcurrentNum; i++) {
                List temp = new ArrayList();
                int start = i * len;
                int end = len + start;
                if (i == socialTouchConcurrentNum - 1)
                    end = list.size();
                for (int j = start; j < end; j++) {
                    temp.add(list.get(j));
                }
                lists[i] = temp;
            }
        }else{
            lists[0] = list;
        }
        return lists;
    }

    private void updatePushInfo(int num, PushDetail pushDetail) {
        Long pushDate = 0L;
        Integer status = 1;
        if(num==2){//第三次
            pushDate = new Date().getTime()/1000;
            status = 3;
        }
        sendMapper.updatePushInfoStatusById(pushDetail.getId(),pushDate,status);
        sendMapper.updatePushInfoRetryById(pushDetail.getId());
    }

    private String getDateStr(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(new Date());
    }

    public int getUserNum(){
        return NUM;
    }

}
