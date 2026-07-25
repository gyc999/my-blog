package com.gyc.blog.service;

import com.gyc.blog.entity.Comment;
import com.gyc.blog.entity.vo.CommentVO;
import java.util.List;

public interface CommentService {
    boolean publishComment(Comment comment, Long currentUserId);
    List<CommentVO> getCommentsByArticleId(Long articleId);
    boolean deleteComment(Long commentId, Long currentUserId);
}