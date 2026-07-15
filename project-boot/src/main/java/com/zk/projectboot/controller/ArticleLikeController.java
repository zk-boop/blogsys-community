package com.zk.projectboot.controller;

import com.zk.projectboot.application.InteractionApplicationService;
import com.zk.projectboot.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
public class ArticleLikeController {

    private final InteractionApplicationService interactionService;

    public ArticleLikeController(InteractionApplicationService interactionService) {
        this.interactionService = interactionService;
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> check(@RequestParam Integer articleId) {
        return ResponseEntity.ok(ApiResponse.success(interactionService.isLiked(articleId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> like(@RequestParam Integer articleId) {
        boolean created = interactionService.like(articleId);
        return ResponseEntity.ok(created
                ? ApiResponse.success("点赞成功")
                : ApiResponse.success("已点赞，无需重复操作"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> unlike(@RequestParam Integer articleId) {
        boolean removed = interactionService.unlike(articleId);
        return ResponseEntity.ok(removed
                ? ApiResponse.success("取消点赞成功")
                : ApiResponse.success("未点赞，无需取消"));
    }
}
