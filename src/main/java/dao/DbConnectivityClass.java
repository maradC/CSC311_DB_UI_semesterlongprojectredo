package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Major;
import model.Person;
import service.MyLogger;

import java.sql.*;
import java.util.List;

import static viewmodel.MainApplication.cnUtil;

public class DbConnectivityClass {

    private List<Person> users;

    final static String DB_NAME = "CSC311_BD_TEMP";
    MyLogger lg = new MyLogger();
    final static String SQL_SERVER_URL = "jdbc:mysql://maradcsc11.mysql.database.azure.com"; // update this server name
    final static String DB_URL = "jdbc:mysql://maradcsc11.mysql.database.azure.com/" + DB_NAME; // update this database name
    final static String USERNAME = "marac"; // update this username
    final static String PASSWORD = "Forcsc311"; // update this password

    private final ObservableList<Person> data = FXCollections.observableArrayList();

    // Method to retrieve all data from the database and store it into an observable list to use in the GUI tableview.
    public ObservableList<Person> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                lg.makeLog("No data");
            }

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");

                // Use fromString() to handle special cases
                String majorString = resultSet.getString("major");
                Major major = Major.fromString(majorString);  // Use the fromString method to handle special cases

                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");

                // Create and add new Person object to the list
                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }

            preparedStatement.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }


    public boolean connectToDatabase() {
        boolean hasRegistredUsers = false;
        try {
            // First, connect to MYSQL server and create the database if not created
            Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + "");
            statement.close();
            conn.close();

            // Second, connect to the database and create the table "users" if not created
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            statement = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                    "first_name VARCHAR(200) NOT NULL, " +
                    "last_name VARCHAR(200) NOT NULL, " +
                    "department VARCHAR(200), " +
                    "major VARCHAR(200), " +
                    "email VARCHAR(200) NOT NULL UNIQUE, " +
                    "imageURL VARCHAR(200))";
            statement.executeUpdate(sql);

            // Check if we have users in the table "users"
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegistredUsers = true;
                }
            }

            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasRegistredUsers;
    }

    public void queryUserByLastName(String name) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users WHERE last_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String major = resultSet.getString("major");
                String department = resultSet.getString("department");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + ", Major: " + major + ", Department: " + department);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listAllUsers() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + ", Department: " + department + ", Major: " + major + ", Email: " + email);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertUser(Person person) {
        String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            // Prepare the statement and specify we want the generated keys (AUTO_INCREMENT id)
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Set the values in the PreparedStatement
            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, person.getMajor().toString());  // Store the Major enum as a string
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());

            // Execute the query
            int affectedRows = preparedStatement.executeUpdate();

            // Check if we have any generated keys (the auto-incremented id)
            if (affectedRows > 0) {
                // Get the generated keys (i.e., the auto-incremented id)
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1); // Get the ID from the first column
                    person.setId(generatedId);  // Set the generated ID on the Person object

                    // Log the generated ID for debugging purposes
                    System.out.println("Generated ID: " + generatedId);
                }
            }

            // Log the successful insertion of the new user
            MyLogger.makeLog("A new user was inserted successfully with ID: " + person.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void editUser(int id, Person p) {
        // Ensure you have a valid ID
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid ID");
        }

        // SQL query for updating user data
        String sql = "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            // Set the new values to the prepared statement
            preparedStatement.setString(1, p.getFirstName());
            preparedStatement.setString(2, p.getLastName());
            preparedStatement.setString(3, p.getDepartment());
            preparedStatement.setString(4, p.getMajor().toString());  // Update Major as string
            preparedStatement.setString(5, p.getEmail());
            preparedStatement.setString(6, p.getImageURL());
            preparedStatement.setInt(7, id);  // Ensure the correct ID is passed in

            // Execute the update query
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Successfully updated the user
                System.out.println("User updated successfully with ID: " + id);
                MyLogger.makeLog("User updated successfully with ID: " + id);
            } else {
                System.out.println("No user found with ID: " + id);
                MyLogger.makeLog("No user found with ID: " + id);
            }

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            MyLogger.makeLog("Error updating user: " + e.getMessage());
        }
    }


    public void deleteRecord(Person person) {
        int id = person.getId();
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to retrieve id from database where it is auto-incremented.
    public int retrieveId(Person p) {
        connectToDatabase();
        int id = -1;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        lg.makeLog(String.valueOf(id));
        return id;
    }

    // Ensure this happens somewhere in the code
    public static DbConnectivityClass cnUtil = new DbConnectivityClass();

    // Method to retrieve all users for CSV export
    public String stringAllUsers() {
        List<Person> users = cnUtil.getUsers(); // Corrected to getUsers()

        // StringBuilder for building CSV data
        StringBuilder csvData = new StringBuilder();

        // CSV Header
        csvData.append("id,firstName,lastName,department,major,email,imageURL\n");

        // Loop through each user and append their data as a CSV row
        for (Person user : users) {
            csvData.append(safe(user.getId())).append(",")
                    .append(safe(user.getFirstName())).append(",")
                    .append(safe(user.getLastName())).append(",")
                    .append(safe(user.getDepartment())).append(",")
                    .append(safe(user.getMajor())).append(",")
                    .append(safe(user.getEmail())).append(",")
                    .append(safe(user.getImageURL())).append("\n");
        }

        // Return the CSV data as a string
        return csvData.toString();
    }

    // Helper method to handle null or empty values
    private String safe(Object value) {
        return value == null ? "" : value.toString();  // Return empty string if value is null
    }

    public List<Person> getUsers() {
        return users;
    }

    public void setUsers(List<Person> users) {
        this.users = users;
    }
}
