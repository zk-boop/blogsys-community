package com.zk.projectboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目启动类
 * 这是整个Spring Boot应用程序的入口点
 * @SpringBootApplication注解结合了@Configuration、@EnableAutoConfiguration和@ComponentScan功能
 * 它自动配置Spring应用程序，扫描组件并注册bean
 */
@SpringBootApplication
@EnableScheduling
public class ProjectBootApplication {

    /**
     * 应用程序的主方法，程序执行的起点
     * 负责启动Spring Boot应用程序
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 启动Spring应用上下文，加载所有配置和bean定义
        SpringApplication.run(ProjectBootApplication.class, args);
    }
}
