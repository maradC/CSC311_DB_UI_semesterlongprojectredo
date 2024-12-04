package viewmodel;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class AboutController {
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ImageView aboutImageView;

    @FXML
    private Label brandNameLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private void initialize() {
        // Disable window resizing by getting the stage and setting resizable to false
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setResizable(false);
    }



}
