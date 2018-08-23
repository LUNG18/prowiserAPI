package com.prowiser.api.server.impl;

import com.prowiser.api.mapper.SignMapper;
import com.prowiser.api.server.SignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SignServiceImpl implements SignService{

    private static Logger log = LoggerFactory.getLogger(SignServiceImpl.class);

    @Resource
    private SignMapper signMapper;

    @Override
    public String getSignBySign(String sign) {
        return signMapper.selectSignBySign(sign);
    }
}
