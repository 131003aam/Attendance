package com.attendance.management.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 注意：当前配置禁用了CSRF保护，仅用于开发环境，生产环境应启用
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // 禁用 CSRF 以允许 POST 请求（开发环境）
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/api/register").permitAll()
                .requestMatchers("/api/init-admin").permitAll()  // 测试接口，生产环境应移除
                .requestMatchers("/api/attendance/**").permitAll()  // 放行所有考勤相关接口
                .requestMatchers("/api/positions/**").permitAll()   // 放行职务配置相关接口
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 管理员专用接口
                .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")  // 员工接口
                .anyRequest().authenticated()
        );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
