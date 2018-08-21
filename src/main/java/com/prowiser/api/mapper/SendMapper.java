package com.prowiser.api.mapper;

import com.prowiser.api.pojo.PushDetail;
import com.prowiser.api.pojo.SignDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface SendMapper {

    @Insert("insert into tb_api_push(user_info,temp_id,sign_id,created_at) values (#{userInfo},#{tempId},#{sid},#{createAt})")
    void insertSendInfo(@Param("tempId") String tempId, @Param("userInfo") String userInfo, @Param("sid") int sid, @Param("createAt") Long createAt);

    @Select("select * from tb_api_push where retry=#{retry} and status<2 order by status desc limit 0,#{size}")
    List<PushDetail> selectPushDetailByRetryAndStatus(@Param("retry")Integer num,@Param("size")Integer size);

    @Update("update tb_api_push set status=#{status},push_at=#{pushAt} where id=#{id}")
    void updatePushInfoStatusById(@Param("id") Integer id, @Param("pushAt") Long pushAt, @Param("status") Integer status);

    @Update("update tb_api_push set retry=retry+1 where id=#{id}")
    void updatePushInfoRetryById(Integer id);

    Integer selectPushInfoNumByStatus(@Param("status") int status, @Param("sid") int sid);

    List<PushDetail> selectPushInfoByStatus(@Param("status") int status, @Param("sid") int sid);

    int insertSendSign(SignDetail signDetail);

    @Select("select * from tb_api_sign where sign=#{sign}")
    SignDetail selectSignBySign(String sign);
}
