package com.zk.projectboot.controller;

import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Value("${file.avatar-dir}")
    private String avatarDir;

    @Value("${file.cover-dir}")
    private String coverDir;

    private final FileStorageService fileStorageService;

    @Autowired
    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeAvatar(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/avatar/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(ApiResponse.success("头像上传成功", fileDownloadUri));
    }

    @PostMapping("/upload/cover")
    public ResponseEntity<ApiResponse<String>> uploadCover(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeCoverImage(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/cover/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(ApiResponse.success("封面图片上传成功", fileDownloadUri));
    }

    @GetMapping("/avatar/{fileName:.+}")
    public ResponseEntity<Resource> downloadAvatar(@PathVariable String fileName, HttpServletRequest request) {
        return downloadFile(fileName, avatarDir, request);
    }

    @GetMapping("/cover/{fileName:.+}")
    public ResponseEntity<Resource> downloadCover(@PathVariable String fileName, HttpServletRequest request) {
        return downloadFile(fileName, coverDir, request);
    }

    private ResponseEntity<Resource> downloadFile(String fileName, String directory, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName, directory);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // 默认内容类型
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/avatar/{fileName:.+}")
    public ResponseEntity<ApiResponse<Void>> deleteAvatar(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName, avatarDir);
        return ResponseEntity.ok(ApiResponse.success("头像删除成功", null));
    }

    @DeleteMapping("/cover/{fileName:.+}")
    public ResponseEntity<ApiResponse<Void>> deleteCover(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName, coverDir);
        return ResponseEntity.ok(ApiResponse.success("封面图片删除成功", null));
    }
}
