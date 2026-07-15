package com.zk.projectboot.ratelimit;

import java.time.Duration;

enum RateLimitPolicy {
    REGISTER_IP("register", 5, Duration.ofHours(1), Subject.IP),
    LOGIN_IP("login", 20, Duration.ofMinutes(10), Subject.IP),
    CREATE_ARTICLE_USER("create-article", 10, Duration.ofDays(1), Subject.USER),
    SUBMIT_ARTICLE_USER("submit-article", 10, Duration.ofDays(1), Subject.USER),
    COMMENT_USER("comment", 30, Duration.ofHours(1), Subject.USER),
    UPLOAD_USER("upload", 20, Duration.ofHours(1), Subject.USER);

    private final String keyPrefix;
    private final int limit;
    private final Duration window;
    private final Subject subject;

    RateLimitPolicy(String keyPrefix, int limit, Duration window, Subject subject) {
        this.keyPrefix = keyPrefix;
        this.limit = limit;
        this.window = window;
        this.subject = subject;
    }

    String getKeyPrefix() {
        return keyPrefix;
    }

    int getLimit() {
        return limit;
    }

    Duration getWindow() {
        return window;
    }

    Subject getSubject() {
        return subject;
    }

    enum Subject {
        IP,
        USER
    }
}
