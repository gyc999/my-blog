package com.gyc.blog.controller;

import com.gyc.blog.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.path:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只允许上传图片文件");
        }
        try {
            // 按日期创建子目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = Paths.get(uploadDir, dateDir);
            Files.createDirectories(uploadPath);

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + ext;
            Path filePath = uploadPath.resolve(newFileName);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 返回访问路径（与 WebConfig 中 /uploads/** 映射对应）
            String accessPath = "/uploads/" + dateDir + "/" + newFileName;
            Map<String, String> result = new HashMap<>();
            result.put("url", accessPath);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
