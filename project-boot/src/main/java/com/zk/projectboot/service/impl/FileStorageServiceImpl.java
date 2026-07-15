package com.zk.projectboot.service.impl;

import com.zk.projectboot.exception.FileStorageException;
import com.zk.projectboot.exception.FileNotFoundException;
import com.zk.projectboot.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    ));
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    ));

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.avatar-dir}")
    private String avatarDir;

    @Value("${file.cover-dir}")
    private String coverDir;

    private Path fileStorageLocation;
    private Path avatarStorageLocation;
    private Path coverStorageLocation;

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            this.avatarStorageLocation = Paths.get(avatarDir).toAbsolutePath().normalize();
            this.coverStorageLocation = Paths.get(coverDir).toAbsolutePath().normalize();

            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(avatarStorageLocation);
            Files.createDirectories(coverStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("无法创建文件上传目录", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String directory) {
        validateImage(file);
        String fileName = generateFileName(file.getOriginalFilename());

        try {
            Path root = getTargetLocation(directory);
            Path targetLocation = resolveInsideRoot(root, fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("无法存储文件 " + fileName, ex);
        }
    }

    @Override
    public String storeAvatar(MultipartFile file) {
        return storeFile(file, avatarDir);
    }

    @Override
    public String storeCoverImage(MultipartFile file) {
        return storeFile(file, coverDir);
    }

    @Override
    public Resource loadFileAsResource(String fileName, String directory) {
        try {
            Path filePath = resolveInsideRoot(getTargetLocation(directory), fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("文件未找到: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("文件未找到: " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileName, String directory) {
        try {
            Path filePath = resolveInsideRoot(getTargetLocation(directory), fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("无法删除文件 " + fileName, ex);
        }
    }

    @Override
    public Path getFileLocation(String fileName, String directory) {
        return resolveInsideRoot(getTargetLocation(directory), fileName);
    }

    private Path getTargetLocation(String directory) {
        if (directory.equals(avatarDir)) {
            return avatarStorageLocation;
        } else if (directory.equals(coverDir)) {
            return coverStorageLocation;
        }
        return fileStorageLocation;
    }

    private String generateFileName(String originalFileName) {
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        }

        return UUID.randomUUID().toString() + extension;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("上传文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new FileStorageException("仅支持 JPEG、PNG、GIF 或 WebP 图片");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new FileStorageException("文件缺少有效扩展名");
        }
        String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("不支持的图片扩展名");
        }
    }

    private Path resolveInsideRoot(Path root, String fileName) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path candidate = normalizedRoot.resolve(fileName).normalize();
        if (!candidate.startsWith(normalizedRoot)) {
            throw new FileStorageException("非法文件路径");
        }
        return candidate;
    }
}
