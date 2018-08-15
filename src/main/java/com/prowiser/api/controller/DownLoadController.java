package com.prowiser.api.controller;

import com.prowiser.api.server.DownLoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("download")
public class DownLoadController {
    private static Logger log = LoggerFactory.getLogger(DownLoadController.class);

    @Resource
    private DownLoadService downLoadService;

    @RequestMapping("msg/log")
    @ResponseBody
    public String info(HttpServletRequest request, HttpServletResponse response,String sign){
        log.info("log sign = "+sign);
        if(StringUtils.isEmpty(sign)){
            return "Parameter : sign is null";
        }else {
            return downLoadService.excel2Client(request, response, sign);
        }
    }
}
