package com.mulehang.blog.converter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArticleConverterBeanTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ArticleConverterBeanTest.ScanConfig.class);

    @Test
    void shouldRegisterArticleConverterBean() {
        contextRunner.run(context -> {
            ArticleConverter converter = context.getBean(ArticleConverter.class);
            assertNotNull(converter);
        });
    }

    @Configuration
    @ComponentScan("com.mulehang.blog.converter")
    static class ScanConfig {
    }
}