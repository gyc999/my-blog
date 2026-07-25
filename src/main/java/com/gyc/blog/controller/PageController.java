package com.gyc.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index"; // 对应 templates/index.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/article/detail/{id}")
    public String detail() {
        return "detail"; // 文章详情页
    }

    @GetMapping("/article/publish")
    public String publish() {
        return "publish"; // 发布文章页
    }

    @GetMapping("/article/edit/{id}")
    public String edit() {
        return "edit"; // 编辑文章页
    }

    @GetMapping("/user/profile")
    public String profile() {
        return "profile"; // 个人主页
    }
}