package example.deploy.hotdeploy.client;

public enum Major {
    INPUT_TEMPLATE(14, "inputtemplate"), ARTICLE(1, "article"), DEPARTMENT(2, "department"), UNKNOWN(-1, "unknown");

    private static Major[] ALL_MAJORS;
    private int integerMajor;
    private String name;

    private Major(int integerMajor, String name) {
        this.integerMajor = integerMajor;
        this.name = name;
    }

    public static Major getMajor(String majorName) {
        for (Major major : ALL_MAJORS) {
            if (major.getName().equalsIgnoreCase(majorName) ||
                    Integer.toString(major.getIntegerMajor()).equals(majorName)) {
                return major;
            }
        }

        return UNKNOWN;
    }

    public int getIntegerMajor() {
        return integerMajor;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    static {
        ALL_MAJORS = new Major[] {Major.ARTICLE, DEPARTMENT, INPUT_TEMPLATE};
    }
}
