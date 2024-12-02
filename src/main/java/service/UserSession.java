package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static UserSession instance;

    private String userName;
    private String password;
    private String privileges;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;

        Preferences userPreferences = Preferences.userRoot().node(this.getClass().getName());
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    public static UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            instance = new UserSession(userName, password, privileges);
        }
        return instance;
    }

    public static UserSession getInstance(String userName, String password) {
        if (instance == null) {
            instance = new UserSession(userName, password, "USER");
        }
        return instance;
    }

    public static String getSavedUsername() {
        Preferences userPreferences = Preferences.userRoot().node(UserSession.class.getName());
        return userPreferences.get("USERNAME", null);  // Return null if not found
    }

    public static String getSavedPassword() {
        Preferences userPreferences = Preferences.userRoot().node(UserSession.class.getName());
        return userPreferences.get("PASSWORD", null);  // Return null if not found
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

    public void cleanUserSession() {
        this.userName = "";
        this.password = "";
        this.privileges = "";
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges=" + this.privileges +
                '}';
    }
}
