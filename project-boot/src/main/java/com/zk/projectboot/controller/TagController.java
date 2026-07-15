package com.zk.projectboot.controller;

import com.zk.projectboot.application.TaxonomyApplicationService;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.dto.TagDTO;
import com.zk.projectboot.dto.TagWriteRequest;
import com.zk.projectboot.exception.BusinessException;
import com.zk.projectboot.model.Tag;
import com.zk.projectboot.service.TagService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;
    private final TaxonomyApplicationService taxonomyService;

    public TagController(TagService tagService, TaxonomyApplicationService taxonomyService) {
        this.tagService = tagService;
        this.taxonomyService = taxonomyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagDTO>> getById(@PathVariable Integer id) {
        Tag tag = tagService.getTagById(id)
                .filter(value -> Integer.valueOf(1).equals(value.getStatus()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "标签不存在"));
        return ResponseEntity.ok(ApiResponse.success(TagDTO.fromTag(tag)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(toDtos(tagService.getAllApprovedTags())));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TagDTO>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(tagService.searchApprovedTags(
                        keyword, PageRequest.of(page, size)).getContent().stream()
                .map(TagDTO::fromTag).collect(Collectors.toList())));
    }

    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<TagDTO>>> hot(@RequestParam(defaultValue = "10") int limit) {
        List<TagDTO> tags = tagService.getHotTags(limit).stream()
                .filter(tag -> Integer.valueOf(1).equals(tag.getStatus()))
                .map(TagDTO::fromTag)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<ApiResponse<List<TagDTO>>> byArticle(@PathVariable Integer articleId) {
        List<TagDTO> tags = tagService.getTagsByArticleId(articleId).stream()
                .filter(tag -> Integer.valueOf(1).equals(tag.getStatus()))
                .map(TagDTO::fromTag)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagDTO>> create(@Valid @RequestBody TagWriteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(TagDTO.fromTag(taxonomyService.submitTag(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagDTO>> update(
            @PathVariable Integer id,
            @Valid @RequestBody TagWriteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(TagDTO.fromTag(taxonomyService.updateTag(id, request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        taxonomyService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.success("标签已删除", null));
    }

    private List<TagDTO> toDtos(List<Tag> tags) {
        return tags.stream().map(TagDTO::fromTag).collect(Collectors.toList());
    }
}
