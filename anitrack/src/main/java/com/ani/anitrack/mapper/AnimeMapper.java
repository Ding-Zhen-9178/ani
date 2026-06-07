package com.ani.anitrack.mapper;

import com.ani.anitrack.entity.Anime;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnimeMapper {

    @Insert("INSERT INTO anime(user_id, name, current_ep, total_ep, website_url, update_day, rating, last_watch_date, status, cover_url) " +
            "VALUES(#{userId}, #{name}, #{currentEp}, #{totalEp}, #{websiteUrl}, #{updateDay}, #{rating}, #{lastWatchDate}, #{status}, #{coverUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Anime anime);

    @Update("UPDATE anime SET name=#{name}, current_ep=#{currentEp}, total_ep=#{totalEp}, " +
            "website_url=#{websiteUrl}, update_day=#{updateDay}, rating=#{rating}, last_watch_date=#{lastWatchDate}, status=#{status}, cover_url=#{coverUrl} " +
            "WHERE id=#{id} AND user_id=#{userId}")
    int update(Anime anime);

    @Delete("DELETE FROM anime WHERE id=#{id} AND user_id=#{userId}")
    int deleteById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("SELECT * FROM anime WHERE id = #{id} AND user_id = #{userId}")
    Anime selectById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("SELECT * FROM anime WHERE user_id = #{userId} ORDER BY update_day ASC")
    List<Anime> selectAllOrderByUpdateDay(@Param("userId") Integer userId);

    @Update("UPDATE anime SET current_ep = current_ep + 1, last_watch_date = DATE('now') WHERE id = #{id} AND user_id = #{userId} AND (total_ep IS NULL OR current_ep < total_ep)")
    int incrementEp(@Param("id") Integer id, @Param("userId") Integer userId);

    @Update("UPDATE anime SET current_ep = total_ep, status = 'completed', last_watch_date = DATE('now') WHERE id = #{id} AND user_id = #{userId}")
    int markCompleted(@Param("id") Integer id, @Param("userId") Integer userId);

    @Update("UPDATE anime SET status = #{status} WHERE id = #{id} AND user_id = #{userId}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status, @Param("userId") Integer userId);
}