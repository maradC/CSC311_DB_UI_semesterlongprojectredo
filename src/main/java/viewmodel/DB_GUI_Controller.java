package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Major;
import model.Person;
import service.MyLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {
    @FXML
    public Label statusMessageLabel;
    public Button clearBtn;
    public Button addBtn;
    public Button delBtn;
    public Button editBtn;

    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ComboBox<Major> majorComboBox;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    @FXML
    private ProgressBar progressBar;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();


    StorageUploader store = new StorageUploader();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            // Populate the ComboBox with Major enum values
            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));

            // Optionally, you can set a default value for the ComboBox
            majorComboBox.getSelectionModel().selectFirst();

            // Disabling buttons when no item is selected
            editBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            delBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
        } catch (Exception e) {
            throw new RuntimeException("Error initializing the form", e);
        }
    }

    @FXML
    protected void addNewRecord() {
        // Get the selected Major from the ComboBox
        Major selectedMajor = majorComboBox.getValue();

        // Get data from TextFields and create a new Person object
        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                selectedMajor, email.getText(), imageURL.getText());

        // Add the new person to the data list
        data.add(p);

        // Log the successful addition
        MyLogger.makeLog("New record added: " + p);

        // Insert the new user into the database
        DbConnectivityClass.cnUtil.insertUser(p); // This will insert the user and set the generated ID

        // Clear the form after adding the record
        clearForm();

        // Update the status message
        updateStatusMessage("User added successfully!");
    }

    // Method to validate the form
    private boolean isFormValid() {

        // Validate first name (only letters, and hyphen allowed)
        String firstName = first_name.getText();
        if (!firstName.matches("^[A-Za-z]+(-[A-Za-z]+)*$")) {
            return false;
        }

        // Validate last name (only letters, and hyphen allowed)
        String lastName = last_name.getText();
        if (!lastName.matches("^[A-Za-z]+(-[A-Za-z]+)*$")) {
            return false;
        }

        // Validate email (standard email format)
        String emailAddress = email.getText();
        if (!emailAddress.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return false;
        }

        // Validate department (non-empty, any characters allowed)
        String departmentField = department.getText();
        if (departmentField.isEmpty()) {
            return false;
        }

        // Validate major (ensure it matches predefined values like Business, CSC, CPIS)
        Major majorField = majorComboBox.getValue();
        if (majorField == null) {
            return false;
        }

        // If all checks pass, the form is valid
        return true;
    }


    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        majorComboBox.getSelectionModel().clearSelection();
        email.setText("");
        imageURL.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();

        if (selectedPerson == null) {
            updateErrorStatusMessage("No user is selected for editing.");
            return;
        }

        String updatedFirstName = first_name.getText();
        String updatedLastName = last_name.getText();
        String updatedDepartment = department.getText();
        Major updatedMajor = majorComboBox.getSelectionModel().getSelectedItem();  // Get selected Major from ComboBox
        String updatedEmail = email.getText();
        String updatedImageURL = imageURL.getText();

        Person updatedPerson = new Person(
                selectedPerson.getId(),
                updatedFirstName,
                updatedLastName,
                updatedDepartment,
                updatedMajor,
                updatedEmail,
                updatedImageURL
        );

        try {
            // Perform the update operation in the database
            cnUtil.editUser(selectedPerson.getId(), updatedPerson);

            // If no exception was thrown, assume update is successful
            int index = tv.getSelectionModel().getSelectedIndex();
            data.set(index, updatedPerson);  // Update the table data
            tv.getSelectionModel().select(index);  // Keep the selection on the updated row

            // Log the update operation
            MyLogger.makeLog("User updated: " + updatedPerson.getFirstName() + " " + updatedPerson.getLastName());

            // Show success message
            updateStatusMessage("User updated successfully!");

            clearForm();
        } catch (Exception e) {
            // If an exception occurs, update the status message with an error
            updateErrorStatusMessage("An error occurred while updating the user details.");
            e.printStackTrace();
        }
    }


    private void updateStatusMessage(String message) {
        statusMessageLabel.setText(message);  // Set the message to the statusMessageLabel
        statusMessageLabel.setStyle("-fx-text-fill: green;");  // Optional: set text color to green for success
    }
    private void updateErrorStatusMessage(String message) {
        statusMessageLabel.setText(message);  // Set the message to the statusMessageLabel
        statusMessageLabel.setStyle("-fx-text-fill: red;");  // Set text color to red for errors
    }



    private void showAlert(Alert.AlertType alertType, String noSelection, String s) {
        Alert alert = new Alert(alertType);
        alert.setTitle(noSelection);
        alert.setContentText(s);
        alert.showAndWait();
    }

    @FXML
    protected void deleteRecord() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();

        if (selectedPerson == null) {
            MyLogger.makeLog("ERROR: No user selected for deletion.");
            updateErrorStatusMessage("No user is selected for deletion.");
            return;
        }

        int selectedIndex = tv.getSelectionModel().getSelectedIndex();

        try {
            MyLogger.makeLog("Attempting to delete user: " + selectedPerson.getFirstName() + " " + selectedPerson.getLastName() + " | ID: " + selectedPerson.getId());
            cnUtil.deleteRecord(selectedPerson);

            data.remove(selectedIndex);
            tv.getSelectionModel().clearSelection();

            MyLogger.makeLog("Successfully deleted user: " + selectedPerson.getFirstName() + " " + selectedPerson.getLastName() + " | ID: " + selectedPerson.getId());
            updateStatusMessage("User deleted successfully!");

            if (data.isEmpty()) {
                delBtn.setDisable(true);
            }
            clearForm();

        } catch (Exception e) {
            MyLogger.makeLog("ERROR: Failed to delete user. Exception: " + e.getMessage());
            updateErrorStatusMessage("An error occurred while deleting the user.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        majorComboBox.getSelectionModel().select(p.getMajor());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }
    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100); // Update progress
                    }
                } catch (IOException e) {
                    // Handle IO errors (e.g., file read errors, network issues)
                    updateMessage("Error uploading file: " + e.getMessage());
                    updateProgress(0, 100);  // Reset progress on failure
                }

                return null;
            }
        };
    }

}