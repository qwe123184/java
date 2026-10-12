package com.fenglin.springboottest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc / OpenAPI 3 文档配置。
 * 启动后访问：http://localhost:8081/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI zhanShenOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("斩神之凡尘神域 · 后端 API")
                        .description("用户注册 / 登录 / 域境（realm）相关接口文档")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("fenglin")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("Proprietary")));
    }
}
