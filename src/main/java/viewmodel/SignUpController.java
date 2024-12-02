package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserSession;

public class SignUpController {

    @FXML
    public PasswordField passwordField;

    @FXML
    public TextField emailField;

    @FXML
    public TextField lastName;

    @FXML
    public TextField firstName;

    @FXML
    private Button goBackBtn;

    @FXML
    private Button newAccountBtn;


    public void createNewAccount(ActionEvent actionEvent) {
        String email = emailField.getText();
        String password = passwordField.getText();
        String firstNameText = firstName.getText();
        String lastNameText = lastName.getText();

        if (firstNameText.isEmpty() || lastNameText.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled in.");
            return;
        }
        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Password Error", "Password must be at least 6 characters.");

        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Passwords do not match.");
            return;
        }

        try {
            UserSession userSession = UserSession.getInstance(email, password, "USER");
            showAlert(Alert.AlertType.INFORMATION, "Account Created", "Account created successfully!");
            clearFields();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Account Creation Failed", "An error occurred while creating the account. Please try again.");
        }


        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Info for the user. Message goes here");
        alert.showAndWait();
    }

    private void clearFields() {
        firstName.clear();
        lastName.clear();
        emailField.clear();
        passwordField.clear();

    }


    private void showAlert (Alert.AlertType alertType, String validationError, String s){
        Alert alert = new Alert(alertType);
        alert.setTitle(validationError);
        alert.setContentText(s);
        alert.showAndWait();
    }

        public void goBack (ActionEvent actionEvent){
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                Scene scene = new Scene(root, 900, 600);
                scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
                Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

}


