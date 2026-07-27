package com.gyc.blog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
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
        RestTemplate template = new RestTemplate();
        template.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
            setConnectTimeout(Duration.ofSeconds(10));
            setReadTimeout(Duration.ofSeconds(30));
        }});
        this.restTemplate = template;
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
                // 安全检查：如果摘要和原文几乎一样（模型偷懒），丢弃
                if (isTooSimilar(summary, plainText)) {
                    log.warn("AI 摘要与原文过于相似，丢弃 | {}ms", elapsed);
                    return "";
                }
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
                "请将以下博客文章总结为一句话摘要，不超过%d个字，直接给出摘要内容，不要重复原文，不要加\"这篇文章\"\"本文\"等前缀。\n\n%s",
                maxSummaryChars, text
        );
    }

    @SuppressWarnings("unchecked")
    private String callAnthropic(String prompt) {
        String url = "https://api.anthropic.com/v1/messages";
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxSummaryChars + 50,
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

    /**
     * 检查 AI 返回的摘要是否和原文过于相似（说明模型在偷懒）
     * 用简单的子串匹配：如果摘要全部出现在原文中，或者原文以摘要开头，判定为偷懒
     */
    private boolean isTooSimilar(String summary, String original) {
        String s = summary.replaceAll("\\s+", "").trim();
        String o = original.replaceAll("\\s+", "").trim();
        // 摘要太短没意义
        if (s.length() < 5) return true;
        // 摘要全部包含在原文中 → 模型直接复制了
        if (o.contains(s)) return true;
        // 摘要和原文开头一样 → 模型没做总结
        if (s.length() > 20 && o.startsWith(s.substring(0, Math.min(20, s.length())))) return true;
        return false;
    }
}
