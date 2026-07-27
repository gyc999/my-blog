package com.gyc.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长200个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500, message = "摘要最长500个字符")
    private String summary;
    private String coverImage;
    private Long authorId;
    private Long categoryId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String categoryName;
}