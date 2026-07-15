package com.zk.projectboot.dto;

import com.zk.projectboot.model.Comment;
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
public class CommentDTO {

    private Integer id;
    private String content;
    private Integer articleId;
    private String articleTitle;
    private Integer userId;
    private String username;
    private String userNickname;
    private String userAvatar;
    private Integer parentId;
    private Integer replyToUserId;
    private String replyToUsername;
    private String replyToNickname;
    private Integer likeCount;
    private Comment.CommentStatus status;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies = new ArrayList<>();

    public static CommentDTO fromComment(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());

        if (comment.getArticle() != null) {
            commentDTO.setArticleId(comment.getArticle().getId());
            commentDTO.setArticleTitle(comment.getArticle().getTitle());
        }

        if (comment.getUser() != null) {
            commentDTO.setUserId(comment.getUser().getId());
            commentDTO.setUsername(comment.getUser().getUsername());
            commentDTO.setUserNickname(comment.getUser().getNickname());
            commentDTO.setUserAvatar(comment.getUser().getAvatar());
        }

        if (comment.getParent() != null) {
            commentDTO.setParentId(comment.getParent().getId());
        }

        if (comment.getReplyToUser() != null) {
            commentDTO.setReplyToUserId(comment.getReplyToUser().getId());
            commentDTO.setReplyToUsername(comment.getReplyToUser().getUsername());
            commentDTO.setReplyToNickname(comment.getReplyToUser().getNickname());
        }

        commentDTO.setLikeCount(comment.getLikeCount());
        commentDTO.setStatus(comment.getStatus());
        commentDTO.setCreatedAt(comment.getCreatedAt());

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<CommentDTO> repliesDTO = comment.getReplies().stream()
                    .map(CommentDTO::fromComment)
                    .collect(Collectors.toList());
            commentDTO.setReplies(repliesDTO);
        }

        return commentDTO;
    }
}
