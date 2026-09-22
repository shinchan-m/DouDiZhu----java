package com.itheima.game;

public enum Difficulty {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean usesDeepSeek() {
        return this != EASY;
    }
}
