package com.zk.projectboot.retention;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class VisitorCleanupCompletedEvent {

    private final Set<String> avatarFileNames;
    private final Set<String> coverFileNames;

    public VisitorCleanupCompletedEvent(Set<String> avatarFileNames, Set<String> coverFileNames) {
        this.avatarFileNames = Collections.unmodifiableSet(new LinkedHashSet<>(avatarFileNames));
        this.coverFileNames = Collections.unmodifiableSet(new LinkedHashSet<>(coverFileNames));
    }

    public Set<String> getAvatarFileNames() {
        return avatarFileNames;
    }

    public Set<String> getCoverFileNames() {
        return coverFileNames;
    }
}
