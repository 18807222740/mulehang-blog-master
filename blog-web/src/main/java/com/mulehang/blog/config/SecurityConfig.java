package com.mulehang.blog.config;

import com.mulehang.blog.filter.JwtAuthenticationFilter;
import com.mulehang.blog.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 6 配置
 * 基于 JWT 的无状态认证 + 角色权限控制
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * 配置安全过滤器链
     *
     * @param http HTTP 安全配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 启用 CORS（使用自定义配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 禁用 CSRF（使用 JWT 无需 CSRF 保护）
                .csrf(AbstractHttpConfigurer::disable)

                // Session 管理：无状态
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 异常处理
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // 请求授权配置
                .authorizeHttpRequests(auth -> auth
                        // 公开接口：认证相关
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 公开接口：前台文章列表/详情/搜索、分类/标签/专栏查询
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles",
                                "/api/v1/articles/slug/**",
                                "/api/v1/articles/hot",
                                "/api/v1/articles/search",
                                "/api/v1/articles/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tags/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/columns/**").permitAll()

                        // 公开接口：网站统计（PV/UV）
                        .requestMatchers(HttpMethod.POST, "/api/v1/stats/pv").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stats").permitAll()

                        // Swagger 文档
                        .requestMatchers("/doc.html", "/swagger-ui.html", "/swagger-ui/**",
                                "/webjars/**", "/v3/api-docs/**").permitAll()

                        // Actuator 健康检查
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // WebSocket 端点
                        .requestMatchers("/ws/**").permitAll()

                        // 管理接口（需要 ADMIN 角色）
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )

                // 添加 JWT 过滤器（在 UsernamePasswordAuthenticationFilter 之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     *
     * @return BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
