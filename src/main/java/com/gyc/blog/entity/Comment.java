package com.gyc.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "文章ID不能为空")
    @Min(value = 1, message = "文章ID必须大于0")
    private Long articleId;

    private Long userId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
    private Long replyToUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}