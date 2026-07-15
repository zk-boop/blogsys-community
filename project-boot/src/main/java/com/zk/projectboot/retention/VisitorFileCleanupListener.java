package com.zk.projectboot.retention;

import com.zk.projectboot.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class VisitorFileCleanupListener {

    private static final Logger log = LoggerFactory.getLogger(VisitorFileCleanupListener.class);

    private final FileStorageService fileStorageService;
    private final String avatarDirectory;
    private final String coverDirectory;

    public VisitorFileCleanupListener(FileStorageService fileStorageService,
                                      @Value("${file.avatar-dir}") String avatarDirectory,
                                      @Value("${file.cover-dir}") String coverDirectory) {
        this.fileStorageService = fileStorageService;
        this.avatarDirectory = avatarDirectory;
        this.coverDirectory = coverDirectory;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteFiles(VisitorCleanupCompletedEvent event) {
        event.getAvatarFileNames().forEach(fileName -> deleteSafely(fileName, avatarDirectory));
        event.getCoverFileNames().forEach(fileName -> deleteSafely(fileName, coverDirectory));
    }

    private void deleteSafely(String fileName, String directory) {
        try {
            fileStorageService.deleteFile(fileName, directory);
        } catch (RuntimeException exception) {
            log.warn("Unable to delete retained upload file after database cleanup: {}", fileName);
        }
    }
}
