package com.ani.anitrack.mapper;

import com.ani.anitrack.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(username, password_hash) VALUES(#{username}, #{passwordHash})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(User user);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Integer id);

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int countByUsername(String username);
}