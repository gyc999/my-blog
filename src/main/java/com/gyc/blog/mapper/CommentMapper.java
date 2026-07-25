package com.gyc.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gyc.blog.entity.Comment;
import com.gyc.blog.entity.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    // 查询顶级评论（parent_id = 0）
    @Select("SELECT c.*, u.nickname AS user_nickname, u.avatar AS user_avatar, " +
            "ru.nickname AS reply_to_user_nickname " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.article_id = #{articleId} AND c.parent_id = 0 AND c.deleted = 0 " +
            "ORDER BY c.create_time DESC")
    List<CommentVO> selectTopLevelComments(@Param("articleId") Long articleId);

    // 查询某条评论的所有子回复
    @Select("SELECT c.*, u.nickname AS user_nickname, u.avatar AS user_avatar, " +
            "ru.nickname AS reply_to_user_nickname " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_user_id = ru.id " +
            "WHERE c.parent_id = #{parentId} AND c.deleted = 0 " +
            "ORDER BY c.create_time ASC")
    List<CommentVO> selectChildComments(@Param("parentId") Long parentId);
}