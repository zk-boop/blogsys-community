package com.zk.projectboot.retention;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.ArticleFavorite;
import com.zk.projectboot.model.ArticleLike;
import com.zk.projectboot.model.Comment;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisitorDataCleanupServiceTest {

    @Test
    void deletesOnlyExpiredEphemeralUsersAndReportsTheirAggregate() {
        UserRepository users = mock(UserRepository.class);
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        VisitorDataCleanupService service = new VisitorDataCleanupService(users, events);
        Instant cutoff = Instant.parse("2026-07-08T00:00:00Z");

        User expired = new User();
        expired.setId(101);
        expired.setRetentionPolicy(User.RetentionPolicy.EPHEMERAL);
        expired.setAvatar("https://blog.example/api/files/avatar/avatar.png");
        Article article = new Article();
        article.setCoverImage("https://blog.example/api/files/cover/cover.webp");
        article.getComments().add(new Comment());
        article.getLikes().add(new ArticleLike());
        article.getFavorites().add(new ArticleFavorite());
        expired.getArticles().add(article);
        expired.getComments().add(new Comment());
        expired.getArticleLikes().add(new ArticleLike());
        expired.getArticleFavorites().add(new ArticleFavorite());

        LocalDateTime localCutoff = LocalDateTime.ofInstant(cutoff, ZoneId.of("Asia/Shanghai"));
        when(users.findByRetentionPolicyAndCreatedAtLessThanEqual(
                User.RetentionPolicy.EPHEMERAL, localCutoff)).thenReturn(Arrays.asList(expired));

        VisitorCleanupReport report = service.cleanupExpiredVisitors(cutoff);

        assertEquals(1, report.getUsersDeleted());
        assertEquals(1, report.getArticlesDeleted());
        assertEquals(2, report.getCommentsDeleted());
        assertEquals(2, report.getLikesDeleted());
        assertEquals(2, report.getFavoritesDeleted());
        verify(users).deleteAll(Arrays.asList(expired));
        verify(users).flush();
        verify(events).publishEvent(any(VisitorCleanupCompletedEvent.class));
    }
}
