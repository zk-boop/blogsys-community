package com.zk.projectboot.dto;

import com.zk.projectboot.model.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {

    private Integer id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String coverImage;
    private Integer authorId;
    private String authorName;
    private String authorAvatar;
    private Integer categoryId;
    private String categoryName;
    private Article.ArticleStatus status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isFeatured;
    private Boolean isTop;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private List<TagDTO> tags = new ArrayList<>();

    public static ArticleDTO fromArticle(Article article) {
        if (article == null) {
            return null;
        }

        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(article.getId());
        articleDTO.setTitle(article.getTitle());
        articleDTO.setSlug(article.getSlug());
        articleDTO.setContent(article.getContent());
        articleDTO.setExcerpt(article.getExcerpt());
        articleDTO.setCoverImage(article.getCoverImage());

        if (article.getAuthor() != null) {
            articleDTO.setAuthorId(article.getAuthor().getId());
            articleDTO.setAuthorName(article.getAuthor().getNickname() != null ?
                    article.getAuthor().getNickname() : article.getAuthor().getUsername());
            articleDTO.setAuthorAvatar(article.getAuthor().getAvatar());
        }

        if (article.getCategory() != null) {
            articleDTO.setCategoryId(article.getCategory().getId());
            articleDTO.setCategoryName(article.getCategory().getName());
        }

        articleDTO.setStatus(article.getStatus());
        articleDTO.setViewCount(article.getViewCount());
        articleDTO.setLikeCount(article.getLikeCount());
        articleDTO.setCommentCount(article.getCommentCount());
        articleDTO.setIsFeatured(article.getIsFeatured());
        articleDTO.setIsTop(article.getIsTop());
        articleDTO.setPublishedAt(article.getPublishedAt());
        articleDTO.setCreatedAt(article.getCreatedAt());

        // 处理标签
        if (article.getArticleTags() != null && !article.getArticleTags().isEmpty()) {
            List<TagDTO> tagDTOs = article.getArticleTags().stream()
                    .map(articleTag -> {
                        if (articleTag.getTag() != null) {
                            return TagDTO.fromTag(articleTag.getTag()); // 使用TagDTO.fromTag方法确保包含状态信息
                        }
                        return null;
                    })
                    .filter(tagDTO -> tagDTO != null)
                    .collect(Collectors.toList());
            articleDTO.setTags(tagDTOs);
        }

        return articleDTO;
    }
}
