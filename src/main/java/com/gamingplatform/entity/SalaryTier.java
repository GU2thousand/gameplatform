package com.gamingplatform.entity;

public enum SalaryTier {
    INTERN(1, "Intern", 0, 60),
    JUNIOR_ENGINEER(2, "Junior Engineer", 60, 75),
    MID_LEVEL(3, "Mid-Level", 75, 85),
    SENIOR(4, "Senior", 85, 95),
    STAFF(5, "Staff", 95, Double.MAX_VALUE);

    private final int tier;
    private final String title;
    private final double minInclusive;
    private final double maxExclusive;

    SalaryTier(int tier, String title, double minInclusive, double maxExclusive) {
        this.tier = tier;
        this.title = title;
        this.minInclusive = minInclusive;
        this.maxExclusive = maxExclusive;
    }

    public int getTier() {
        return tier;
    }

    public String getTitle() {
        return title;
    }

    public static SalaryTier fromScore(double score) {
        for (SalaryTier value : values()) {
            if (score >= value.minInclusive && score < value.maxExclusive) {
                return value;
            }
        }
        return INTERN;
    }
}
