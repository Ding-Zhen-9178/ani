package com.ani.anitrack.mapper;

import com.ani.anitrack.entity.Checkin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CheckinMapper {

    @Insert("INSERT INTO checkin(user_id, check_date, count) VALUES(#{userId}, DATE('now'), 1) " +
            "ON CONFLICT(user_id, check_date) DO UPDATE SET count = count + 1")
    int recordCheckin(@Param("userId") Integer userId);

    @Select("SELECT check_date, count FROM checkin WHERE user_id = #{userId} " +
            "AND check_date >= DATE('now', '-365 days') ORDER BY check_date ASC")
    List<Checkin> selectByUserId(@Param("userId") Integer userId);
}