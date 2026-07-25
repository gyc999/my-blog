package com.gyc.blog.service;

import com.gyc.blog.entity.Tag;
import java.util.List;

public interface TagService {
    List<Tag> list();
    List<Tag> getTagsByArticleId(Long articleId);
    boolean add(Tag tag);
    boolean delete(Long id);
}
