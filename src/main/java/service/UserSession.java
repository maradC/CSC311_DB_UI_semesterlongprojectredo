package service;

import java.util.prefs.Preferences;

public class UserSession {

    private String userName;
    private String password;
    private String privileges;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;

        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    // Inner static class responsible for holding the Singleton instance
    private static class UserSessionHolder {
        private static final UserSession INSTANCE = new UserSession(
                Preferences.userRoot().get("USERNAME", ""),
                Preferences.userRoot().get("PASSWORD", ""),
                Preferences.userRoot().get("PRIVILEGES", "NONE")
        );
    }

    public static UserSession getInstance(String userName, String password, String privileges) {
        return new UserSession(userName, password, privileges);
    }

    public static UserSession getInstance() {
        return UserSessionHolder.INSTANCE;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public String getPrivileges() {
        return this.privileges;
    }

    // Method to clean user session data
    public void cleanUserSession() {
        this.userName = ""; // Or null if you prefer
        this.password = "";
        this.privileges = ""; // Or null if you prefer
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges='" + this.privileges + '\'' +
                '}';
    }
}
