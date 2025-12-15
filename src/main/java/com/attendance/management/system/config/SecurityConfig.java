package com.attendance.management.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // 禁用 CSRF 以允许 POST 请求
            .authorizeHttpRequests(authz -> authz
                // 暂时允许所有API请求，因为当前没有实现完整的Spring Security认证
                // 实际项目中应该实现JWT或Session认证，然后根据用户角色进行权限控制
                .requestMatchers("/api/**").permitAll()  // 允许所有API请求
                .anyRequest().permitAll()  // 允许其他请求（如静态资源）
        );
        return http.build();
    }

}
