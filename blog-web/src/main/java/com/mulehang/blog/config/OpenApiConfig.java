package com.mulehang.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置
 * 提供 API 文档的元信息配置
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 文档基本信息
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mulehang Blog API")
                        .version("1.0.0")
                        .description("骡行博客系统 RESTful API 文档")
                        .contact(new Contact()
                                .name("Mulehang")
                                .email("contact@mulehang.com")
                                .url("https://github.com/mulehang"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
