package com.gyc.blog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI 服务 — 调用 Claude / OpenAI API 生成文章摘要
 *
 * 设计决策：
 * - 使用 RestTemplate（Spring Boot 内置，无需额外依赖）
 * - API Key 通过环境变量注入，不写入代码
 * - 调用失败时静默降级，返回空字符串（不影响发布流程）
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final int MAX_CONTENT_CHARS = 2000;

    private final RestTemplate restTemplate;

    @Value("${app.ai.provider:anthropic}")
    private String provider;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:claude-haiku-4-5-20251001}")
    private String model;

    @Value("${app.ai.summary-max-chars:150}")
    private int maxSummaryChars;

    @Value("${app.ai.enabled:true}")
    private boolean enabled;

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 根据文章内容生成摘要
     * @param content 文章 Markdown 内容
     * @return 生成的摘要，失败时返回空字符串
     */
    public String generateSummary(String content) {
        if (!enabled || apiKey == null || apiKey.isBlank() || content == null || content.isBlank()) {
            return "";
        }

        // 去除 Markdown 标记，截取前 N 字符作为 prompt 输入
        String plainText = content
                .replaceAll("#{1,6}\\s", "")
                .replaceAll("[*_~`>\\[\\]()]", "")
                .replaceAll("\\n+", " ")
                .trim();
        if (plainText.length() > MAX_CONTENT_CHARS) {
            plainText = plainText.substring(0, MAX_CONTENT_CHARS);
        }

        String prompt = buildPrompt(plainText);

        try {
            long start = System.currentTimeMillis();
            String summary = switch (provider.toLowerCase()) {
                case "openai" -> callOpenAI(prompt, "https://api.openai.com/v1");
                case "deepseek" -> callOpenAI(prompt, "https://api.deepseek.com/v1");
                default -> callAnthropic(prompt);
            };
            long elapsed = System.currentTimeMillis() - start;

            if (summary != null && !summary.isBlank()) {
                // 截断到最大字数
                if (summary.length() > maxSummaryChars + 20) {
                    summary = summary.substring(0, maxSummaryChars + 20);
                    int lastPeriod = summary.lastIndexOf("。");
                    if (lastPeriod > 0) summary = summary.substring(0, lastPeriod + 1);
                }
                log.info("AI 摘要生成成功 | {}ms | {} chars", elapsed, summary.length());
                return summary;
            }
        } catch (Exception e) {
            log.warn("AI 摘要生成失败，降级为空摘要 | {}", e.getMessage());
        }
        return "";
    }

    private String buildPrompt(String text) {
        return String.format(
                "你是一个博客摘要助手。请用中文将以下文章内容总结为一段不超过%d字的摘要，直接输出摘要内容，不要加任何前缀或说明。\n\n文章内容：\n%s",
                maxSummaryChars, text
        );
    }

    @SuppressWarnings("unchecked")
    private String callAnthropic(String prompt) {
        String url = "https://api.anthropic.com/v1/messages";
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxSummaryChars + 50,
                "system", "你是一个专业的博客摘要生成助手。请简洁准确地总结文章核心内容。",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        if (response.getBody() != null && response.getBody().containsKey("content")) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
            if (content != null && !content.isEmpty()) {
                Object text = content.get(0).get("text");
                return text != null ? text.toString().trim() : "";
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String callOpenAI(String prompt, String baseUrl) {
        String url = baseUrl + "/chat/completions";
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxSummaryChars + 50,
                "messages", List.of(
                        Map.of("role", "system", "content", "你是一个专业的博客摘要生成助手。请简洁准确地总结文章核心内容。"),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    Object text = message.get("content");
                    return text != null ? text.toString().trim() : "";
                }
            }
        }
        return "";
    }
}
