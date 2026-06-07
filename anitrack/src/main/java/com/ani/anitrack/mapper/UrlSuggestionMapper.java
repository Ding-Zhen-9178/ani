package com.ani.anitrack.mapper;

import com.ani.anitrack.entity.UrlSuggestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UrlSuggestionMapper {

    @Select("SELECT * FROM url_suggestion WHERE user_id IS NULL OR user_id = #{userId} ORDER BY pinned DESC, is_preset DESC, id ASC")
    List<UrlSuggestion> findAllVisible(@Param("userId") Integer userId);

    @Insert("INSERT INTO url_suggestion(user_id, label, url, search_url, is_preset) VALUES(#{userId}, #{label}, #{url}, #{searchUrl}, #{isPreset})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UrlSuggestion suggestion);

    @Update("UPDATE url_suggestion SET pinned = CASE WHEN pinned THEN 0 ELSE 1 END WHERE id = #{id}")
    int togglePin(@Param("id") Integer id);

    @Delete("DELETE FROM url_suggestion WHERE id = #{id} AND user_id = #{userId} AND is_preset = 0")
    int deleteCustom(@Param("id") Integer id, @Param("userId") Integer userId);
}
