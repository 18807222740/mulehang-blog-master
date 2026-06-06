package com.mulehang;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 博客系统 Web 应用主类。
 *
 * <p>启用了文件存储、MyBatis Mapper 扫描、定时任务等功能。</p>
 */
@EnableFileStorage
@SpringBootApplication
@MapperScan("com.mulehang.blog.mapper")
@EnableScheduling
public class BlogWebApplication {

    /**
     * 应用程序入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BlogWebApplication.class, args);
    }

}
