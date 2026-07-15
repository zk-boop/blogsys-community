package com.zk.projectboot.retention;

public class VisitorCleanupReport {

    private final int usersDeleted;
    private final int articlesDeleted;
    private final int commentsDeleted;
    private final int likesDeleted;
    private final int favoritesDeleted;

    public VisitorCleanupReport(int usersDeleted, int articlesDeleted, int commentsDeleted,
                                int likesDeleted, int favoritesDeleted) {
        this.usersDeleted = usersDeleted;
        this.articlesDeleted = articlesDeleted;
        this.commentsDeleted = commentsDeleted;
        this.likesDeleted = likesDeleted;
        this.favoritesDeleted = favoritesDeleted;
    }

    public int getUsersDeleted() {
        return usersDeleted;
    }

    public int getArticlesDeleted() {
        return articlesDeleted;
    }

    public int getCommentsDeleted() {
        return commentsDeleted;
    }

    public int getLikesDeleted() {
        return likesDeleted;
    }

    public int getFavoritesDeleted() {
        return favoritesDeleted;
    }
}
