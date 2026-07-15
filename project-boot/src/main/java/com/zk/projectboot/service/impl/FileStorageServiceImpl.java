package com.zk.projectboot.service.impl;

import com.zk.projectboot.exception.FileNotFoundException;
import com.zk.projectboot.exception.FileStorageException;
import com.zk.projectboot.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

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
            fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            avatarStorageLocation = Paths.get(avatarDir).toAbsolutePath().normalize();
            coverStorageLocation = Paths.get(coverDir).toAbsolutePath().normalize();

            Files.createDirectories(fileStorageLocation);
            Files.createDirectories(avatarStorageLocation);
            Files.createDirectories(coverStorageLocation);
        } catch (IOException exception) {
            throw new FileStorageException("无法创建文件上传目录", exception);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String directory) {
        ImageType imageType = validateImage(file);
        String fileName = UUID.randomUUID() + imageType.extensionFor(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            Path root = getTargetLocation(directory);
            Path targetLocation = resolveInsideRoot(root, fileName);
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException exception) {
            throw new FileStorageException("无法存储文件 " + fileName, exception);
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
            }
            throw new FileNotFoundException("文件未找到: " + fileName);
        } catch (MalformedURLException exception) {
            throw new FileNotFoundException("文件未找到: " + fileName, exception);
        }
    }

    @Override
    public void deleteFile(String fileName, String directory) {
        try {
            Path filePath = resolveInsideRoot(getTargetLocation(directory), fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new FileStorageException("无法删除文件 " + fileName, exception);
        }
    }

    @Override
    public Path getFileLocation(String fileName, String directory) {
        return resolveInsideRoot(getTargetLocation(directory), fileName);
    }

    private Path getTargetLocation(String directory) {
        if (directory.equals(avatarDir)) {
            return avatarStorageLocation;
        }
        if (directory.equals(coverDir)) {
            return coverStorageLocation;
        }
        return fileStorageLocation;
    }

    private ImageType validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new FileStorageException("文件缺少有效扩展名");
        }
        String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase(Locale.ROOT);

        byte[] header = new byte[12];
        int bytesRead;
        try (InputStream inputStream = file.getInputStream()) {
            bytesRead = inputStream.read(header);
        } catch (IOException exception) {
            throw new FileStorageException("无法读取上传文件", exception);
        }

        byte[] actualHeader = new byte[Math.max(bytesRead, 0)];
        if (bytesRead > 0) {
            System.arraycopy(header, 0, actualHeader, 0, bytesRead);
        }
        ImageType imageType = detectImageType(actualHeader);
        if (!imageType.matches(file.getContentType(), extension)) {
            throw new FileStorageException("图片内容、类型与扩展名不一致");
        }
        return imageType;
    }

    ImageType detectImageType(byte[] header) {
        if (startsWith(header, 0xFF, 0xD8, 0xFF)) {
            return ImageType.JPEG;
        }
        if (startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return ImageType.PNG;
        }
        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return ImageType.WEBP;
        }
        throw new FileStorageException("仅支持真实的 JPEG、PNG 或 WebP 图片");
    }

    private boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if ((bytes[index] & 0xFF) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private Path resolveInsideRoot(Path root, String fileName) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path candidate = normalizedRoot.resolve(fileName).normalize();
        if (!candidate.startsWith(normalizedRoot)) {
            throw new FileStorageException("非法文件路径");
        }
        return candidate;
    }

    enum ImageType {
        JPEG("image/jpeg", ".jpg", ".jpeg"),
        PNG("image/png", ".png"),
        WEBP("image/webp", ".webp");

        private final String contentType;
        private final String[] extensions;

        ImageType(String contentType, String... extensions) {
            this.contentType = contentType;
            this.extensions = extensions;
        }

        private boolean matches(String declaredContentType, String extension) {
            if (declaredContentType == null
                    || !contentType.equals(declaredContentType.toLowerCase(Locale.ROOT))) {
                return false;
            }
            for (String allowedExtension : extensions) {
                if (allowedExtension.equals(extension)) {
                    return true;
                }
            }
            return false;
        }

        private String extensionFor(String originalName) {
            String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
            for (String allowedExtension : extensions) {
                if (allowedExtension.equals(extension)) {
                    return extension;
                }
            }
            throw new FileStorageException("不支持的图片扩展名");
        }
    }
}
