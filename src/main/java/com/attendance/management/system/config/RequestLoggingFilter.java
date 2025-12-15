package com.attendance.management.system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String method = request.getMethod();
        
        System.out.println("========== 收到请求 ==========");
        System.out.println("方法: " + method);
        System.out.println("路径: " + requestURI);
        System.out.println("查询参数: " + queryString);
        System.out.println("完整URL: " + requestURI + (queryString != null ? "?" + queryString : ""));
        
        filterChain.doFilter(request, response);
        
        System.out.println("响应状态: " + response.getStatus());
        System.out.println("==============================");
    }
}




















