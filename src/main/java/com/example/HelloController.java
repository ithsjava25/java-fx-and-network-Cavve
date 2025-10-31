package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    //hantera användarinteraktion

    private final HelloModel model = new HelloModel();

    @FXML
    private Label messageLabel;

    @FXML
    private Label currentDateAndTime;

    public Button updateButton;

    public Button SendMessageButton;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
        if (currentDateAndTime != null)
            currentDateAndTime.setText(LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        public void updateButtonAction(ActionEvent actionEvent) {
            currentDateAndTime.setText(LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        public void sendMessageAction(ActionEvent actionEvent) {

        }
    }


