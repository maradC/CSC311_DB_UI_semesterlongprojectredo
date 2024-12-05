package model;

public enum Major {

    SCIENCE_AND_TECHNOLOGY("Science & Technology"),
    CPIS("CPIS"),
    BUSINESS("Business"),
    NURSING("Nursing"),
    ART("Art "),
    BIOLOGY("Biology"),
    MECHANICAL_ENGINEERING( "Engineering"),
    ENVIRONMENTAL_SCIENCE("Environmental Science");

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
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
