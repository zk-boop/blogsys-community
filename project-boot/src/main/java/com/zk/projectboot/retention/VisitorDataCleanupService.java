package com.zk.projectboot.retention;

import com.zk.projectboot.model.Article;
import com.zk.projectboot.model.User;
import com.zk.projectboot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class VisitorDataCleanupService {

    private static final Logger log = LoggerFactory.getLogger(VisitorDataCleanupService.class);
    private static final ZoneId RETENTION_ZONE = ZoneId.of("Asia/Shanghai");

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VisitorDataCleanupService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public VisitorCleanupReport cleanupExpiredVisitors(Instant cutoff) {
        LocalDateTime localCutoff = LocalDateTime.ofInstant(cutoff, RETENTION_ZONE);
        List<User> expiredUsers = userRepository.findByRetentionPolicyAndCreatedAtLessThanEqual(
                User.RetentionPolicy.EPHEMERAL, localCutoff);

        int articleCount = 0;
        int commentCount = 0;
        int likeCount = 0;
        int favoriteCount = 0;
        Set<String> avatars = new LinkedHashSet<>();
        Set<String> covers = new LinkedHashSet<>();

        for (User user : expiredUsers) {
            addFileName(avatars, user.getAvatar());
            commentCount += user.getComments().size();
            likeCount += user.getArticleLikes().size();
            favoriteCount += user.getArticleFavorites().size();
            articleCount += user.getArticles().size();
            for (Article article : user.getArticles()) {
                addFileName(covers, article.getCoverImage());
                commentCount += article.getComments().size();
                likeCount += article.getLikes().size();
                favoriteCount += article.getFavorites().size();
            }
        }

        if (!expiredUsers.isEmpty()) {
            userRepository.deleteAll(expiredUsers);
            userRepository.flush();
            eventPublisher.publishEvent(new VisitorCleanupCompletedEvent(avatars, covers));
        }

        VisitorCleanupReport report = new VisitorCleanupReport(expiredUsers.size(), articleCount,
                commentCount, likeCount, favoriteCount);
        log.info("Visitor retention cleanup complete: users={}, articles={}, comments={}, likes={}, favorites={}",
                report.getUsersDeleted(), report.getArticlesDeleted(), report.getCommentsDeleted(),
                report.getLikesDeleted(), report.getFavoritesDeleted());
        return report;
    }

    private void addFileName(Set<String> fileNames, String location) {
        if (location == null || location.trim().isEmpty()) {
            return;
        }
        String normalized = location.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (fileName.matches("[A-Za-z0-9._-]+") && !fileName.contains("..")) {
            fileNames.add(fileName);
        }
    }
}
