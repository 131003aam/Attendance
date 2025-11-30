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
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 管理员专用接口
                .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")  // 员工接口
                .anyRequest().authenticated()
        );
        return http.build();
    }

}
