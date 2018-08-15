package com.prowiser.api.server.impl;

import com.prowiser.api.mapper.SendMapper;
import com.prowiser.api.pojo.PushDetail;
import com.prowiser.api.pojo.SignDetail;
import com.prowiser.api.server.DownLoadService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DownLoadServiceImpl implements DownLoadService {
    private static Logger log = LoggerFactory.getLogger(DownLoadServiceImpl.class);

    @Resource
    private SendMapper sendMapper;


    @Override
    public String excel2Client(HttpServletRequest request, HttpServletResponse response, String sign) {
        return doExcel2Client(request,response,sign);
    }


    private String doExcel2Client(HttpServletRequest request, HttpServletResponse response, String sign) {
        log.info("sign = "+sign);
        SignDetail signDetail= sendMapper.selectSignBySign(sign);
        if(signDetail!=null){
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("总推送数", sendMapper.selectPushInfoNumByStatus(-1,signDetail.getId())+"");
            map.put("成功", sendMapper.selectPushInfoNumByStatus(2,signDetail.getId())+"");
            map.put("失败", sendMapper.selectPushInfoNumByStatus(3,signDetail.getId())+"");
            map.put("未推送", sendMapper.selectPushInfoNumByStatus(0,signDetail.getId())+"");
            map.put("未推送open_id", sendMapper.selectPushInfoByStatus(0,signDetail.getId()));
            map.put("对应tempId", "");

            HSSFWorkbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("日志");
            Row row1 = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            int i = 0;
            for(String key : map.keySet()){
                Cell c1 = row1.createCell(i);
                c1.setCellValue(key);
                if(!"未推送open_id".equals(key)){
                    Cell c2 = row2.createCell(i);
                    c2.setCellValue((String) map.get(key));
                }
                i++;
            }
            List<PushDetail> list = (List<PushDetail>) map.get("未推送open_id");
            for (int j=0; j<list.size(); j++){
                Row row3 = sheet.createRow(j+2);
                try {
                    JSONObject userInfo = new JSONObject(list.get(j).getUserInfo());
                    String openId = userInfo.getString("openid");
                    row3.createCell(4).setCellValue(openId);
                    String tempId = list.get(j).getTempId();
                    row3.createCell(5).setCellValue(tempId);
                } catch (JSONException e) {
                    log.info("json user info formart error",e);
                }
            }
            createExcel2Client(request, response, wb, "log");
            return "success";
        }else {
            return "sign is not exist !";
        }
    }

    private void createExcel2Client(HttpServletRequest request, HttpServletResponse response, HSSFWorkbook wb,String fileName) {
        OutputStream fos = null;
        try {
            fos = response.getOutputStream();
            String userAgent = request.getHeader("USER-AGENT");
            try {
                if(StringUtils.contains(userAgent, "Mozilla")){
                    fileName = new String(fileName.getBytes(), "ISO8859-1");
                }else {
                    fileName = URLEncoder.encode(fileName, "utf8");
                }
            } catch (UnsupportedEncodingException e) {
                log.info("UnsupportedEncodingException",e);
            }

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");// 设置contentType为excel格式
            response.setHeader("Content-Disposition", "Attachment;Filename="+ fileName+".xls");
            wb.write(fos);
            fos.close();
        } catch (FileNotFoundException e) {
            log.info("FileNotFoundException",e);
        } catch (IOException e) {
            log.info("IOException",e);
        }
    }

}
