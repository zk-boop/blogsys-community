package com.zk.projectboot.config;

import com.zk.projectboot.service.FileStorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 测试环境专用配置类，用于提供测试所需的模拟Bean
 */
@TestConfiguration
public class TestConfig {

    /**
     * 提供FileStorageService的模拟实现，避免实际文件操作
     */
    @Bean
    @Primary
    public FileStorageService mockFileStorageService() {
        return new FileStorageService() {
            @Override
            public String storeFile(MultipartFile file, String directory) {
                return "mock-file-name.jpg";
            }

            @Override
            public String storeAvatar(MultipartFile file) {
                return "mock-avatar.jpg";
            }

            @Override
            public String storeCoverImage(MultipartFile file) {
                return "mock-cover.jpg";
            }

            @Override
            public Resource loadFileAsResource(String fileName, String directory) {
                return null; // 模拟实现不需要返回真实资源
            }

            @Override
            public void deleteFile(String fileName, String directory) {
                // 无需实际操作
            }

            @Override
            public Path getFileLocation(String fileName, String directory) {
                return Paths.get("mock/path");
            }
        };
    }
}
