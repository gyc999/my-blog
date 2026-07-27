package com.gyc.blog.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * API 请求日志切面
 * 记录每个 Controller 方法的请求参数、执行耗时和异常信息
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    private static final int MAX_ARG_LENGTH = 200;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void controllerLayer() {}

    @Around("controllerLayer()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String method = joinPoint.getSignature().toShortString();
        String args = formatArgs(joinPoint.getArgs());
        String uri = getRequestUri();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("{} | {} | {} | {}ms", method, uri, args, elapsed);
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("{} | {} | {} | {}ms | ERROR: {}", method, uri, args, elapsed, e.getMessage());
            throw e;
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .filter(a -> !(a instanceof HttpServletRequest))
                .map(a -> {
                    String s = String.valueOf(a);
                    if (s.length() > MAX_ARG_LENGTH) {
                        return s.substring(0, MAX_ARG_LENGTH) + "...";
                    }
                    return s;
                })
                .collect(Collectors.joining(", "));
    }

    private String getRequestUri() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getMethod() + " " + attrs.getRequest().getRequestURI();
            }
        } catch (Exception ignored) {}
        return "";
    }

}
