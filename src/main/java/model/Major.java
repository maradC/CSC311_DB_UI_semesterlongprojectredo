package model;

public enum Major {
    SCIENCE_AND_TECHNOLOGY("Science & Technology"),
    CPIS("CPIS"),
    BUSINESS("Business");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Major fromString(String major) {
        for (Major m : Major.values()) {
            if (m.getDisplayName().equalsIgnoreCase(major)) {
                return m;
            }
        }
        return null; // or throw an exception if necessary
    }

    @Override
    public String toString() {
        return displayName;
    }
}
