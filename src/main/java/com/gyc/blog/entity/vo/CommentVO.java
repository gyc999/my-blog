package com.gyc.blog.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private String content;
    private LocalDateTime createTime;

    // 评论者信息
    private Long userId;
    private String userNickname;
    private String userAvatar;

    // 被回复者信息（@谁）
    private Long replyToUserId;
    private String replyToUserNickname;

    // 子回复列表（递归结构）
    private List<CommentVO> children;
    private Boolean hasChildren;
}