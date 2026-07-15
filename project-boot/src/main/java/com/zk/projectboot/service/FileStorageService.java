package com.zk.projectboot.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    String storeFile(MultipartFile file, String directory);

    String storeAvatar(MultipartFile file);

    String storeCoverImage(MultipartFile file);

    Resource loadFileAsResource(String fileName, String directory);

    void deleteFile(String fileName, String directory);

    Path getFileLocation(String fileName, String directory);
}
