package com.gyc.blog.controller;

import com.gyc.blog.common.Result;
import com.gyc.blog.common.UserContext;
import com.gyc.blog.entity.Comment;
import com.gyc.blog.entity.vo.CommentVO;
import com.gyc.blog.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/publish")
    public Result<String> publish(@Valid @RequestBody Comment comment) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        return commentService.publishComment(comment, userId) ?
                Result.success("评论成功") : Result.error("评论失败");
    }

    // 获取文章评论列表（递归嵌套）
    @GetMapping("/list/{articleId}")
    public Result<List<CommentVO>> list(@PathVariable Long articleId) {
        List<CommentVO> comments = commentService.getCommentsByArticleId(articleId);
        return Result.success(comments);
    }

    // 删除评论（递归删除子评论）
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        return commentService.deleteComment(id, userId) ?
                Result.success("删除成功") : Result.error("删除失败，可能评论不存在或非您的评论");
    }

}