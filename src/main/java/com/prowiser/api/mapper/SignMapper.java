package com.prowiser.api.mapper;

import org.apache.ibatis.annotations.Select;

public interface SignMapper {


    @Select("select sign from tb_api_sign where sign=#{sign}")
    String selectSignBySign(String sign);
}
