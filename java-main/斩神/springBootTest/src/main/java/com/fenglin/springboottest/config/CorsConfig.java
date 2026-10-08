package com.fenglin.springboottest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置（CORS）。
 * 开发期默认放行所有来源（cors.allowed-origins=*）；
 * 生产环境请改为前端真实地址，例如：cors.allowed-origins=http://localhost:5173,https://app.example.com
 * 注意：allowCredentials=true 时不能对响应头使用通配符 *，因此生产期必须显式列出来源。
 * 同时注册令牌校验拦截器（登录/注册接口放行）。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration registration = registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        if ("*".equals(allowedOrigins.trim())) {
            // 开发期：放行所有来源（使用 origin pattern，而非把 * 写进 Access-Control-Allow-Origin）
            registration.allowedOriginPatterns("*");
        } else {
            // 生产期：仅允许显式列出的来源
            registration.allowedOrigins(allowedOrigins.split(","));
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login", "/api/user/register", "/api/user/refresh");
    }
}
