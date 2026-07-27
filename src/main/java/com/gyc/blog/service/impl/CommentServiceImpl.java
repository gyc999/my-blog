package com.gyc.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gyc.blog.entity.Comment;
import com.gyc.blog.entity.vo.CommentVO;
import com.gyc.blog.mapper.CommentMapper;
import com.gyc.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public boolean publishComment(Comment comment, Long currentUserId) {
        comment.setUserId(currentUserId);
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        comment.setCreateTime(LocalDateTime.now());
        return commentMapper.insert(comment) > 0;
    }

    @Override
    public List<CommentVO> getCommentsByArticleId(Long articleId) {
        // 1. 查所有顶级评论
        List<CommentVO> topComments = commentMapper.selectTopLevelComments(articleId);
        // 2. 递归填充子评论
        for (CommentVO top : topComments) {
            top.setChildren(getChildCommentsRecursive(top.getId()));
            top.setHasChildren(top.getChildren() != null && !top.getChildren().isEmpty());
        }
        return topComments;
    }

    // 递归核心逻辑
    private List<CommentVO> getChildCommentsRecursive(Long parentId) {
        List<CommentVO> children = commentMapper.selectChildComments(parentId);
        for (CommentVO child : children) {
            child.setChildren(getChildCommentsRecursive(child.getId()));
            child.setHasChildren(child.getChildren() != null && !child.getChildren().isEmpty());
        }
        return children;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !comment.getUserId().equals(currentUserId)) {
            return false;
        }
        // 递归删除（逻辑删除）
        deleteRecursively(commentId);
        return true;
    }

    private void deleteRecursively(Long commentId) {
        commentMapper.deleteById(commentId); // 逻辑删除（因为有 @TableLogic）
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        List<Comment> children = commentMapper.selectList(wrapper);
        for (Comment child : children) {
            deleteRecursively(child.getId());
        }
    }
}