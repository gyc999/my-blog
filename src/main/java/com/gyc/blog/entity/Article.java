package com.gyc.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;  // 封面图片路径
    private Long authorId;
    private Long categoryId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;   // 新增：收藏数
    private Integer status;         // 0-草稿，1-已发布
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String authorName;  // 作者昵称（非DB字段，查询时填充）
}