package com.gyc.blog.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private Integer viewCount;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;

    // 关联字段（来自 user 表和 category 表）
    private String coverImage;    // 封面图片路径
    private Integer collectCount; // 收藏数
    private String authorName;    // 作者昵称
    private String categoryName;  // 分类名称
    private String tags;          // 标签（逗号分隔的字符串）
}