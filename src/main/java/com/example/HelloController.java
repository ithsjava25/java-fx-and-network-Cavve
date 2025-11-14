package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    public Label messageLabel;

    //hantera användarinteraktion

    private NtfyConnection connection;
    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    public TextArea ChatArea;
    public TextField messageField;
    public ListView<NtfyMessageDto> messageView;


    @FXML
    private void initialize() {
        ChatArea.appendText("Hello World!\n");
        // Inkludera Enter som input för att skicka meddelanden för jag är lat
        messageField.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                sendMessage(null);
                event.consume();
            }
        });
        messageView.setItems(model.getMessages());

        messageView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto message, boolean empty) {
                super.updateItem(message, empty);

                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // HBox-container för meddelande + knapp
                HBox container = new HBox(10);
                container.setPadding(new Insets(5, 10, 5, 10));

                // Label med topic och meddelande
                Label msgLabel = new Label("[" + message.topic() + "] " + message.message());
                msgLabel.setWrapText(true);
                msgLabel.setMaxWidth(300);
                container.getChildren().add(msgLabel);

                // Om det finns en fil att ladda ned
                if (message.attachmentUrl() != null && !message.attachmentUrl().isEmpty()) {
                    Button downloadBtn = new Button("Download File");
                    downloadBtn.setOnAction(e -> {
                        FileChooser fileChooser = new FileChooser();
                        // Förslag på filnamn
                        fileChooser.setInitialFileName(message.fileName() != null ? message.fileName() : "attachment");

                        File dest = fileChooser.showSaveDialog(getScene().getWindow());
                        if (dest != null) {
                            // Låt modellen hantera nedladdningen
                            boolean success = model.downloadFile(message.topic(), message.fileName(), dest);
                            if (!success) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Download Error");
                                alert.setContentText("Failed to download file: " + message.fileName());
                                alert.showAndWait();
                            }
                        }
                    });
                    container.getChildren().add(downloadBtn);
                }

                setGraphic(container);
                setText(null); // text sätts till null eftersom vi använder graphic
            }
        });
    }



    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        String message = messageField.getText();

        if (message != null && !message.isBlank()) {
            model.setMessageToSend(message);
            model.sendMessage();

            ChatArea.appendText("You: " + message + "\n");
            messageField.clear();
        }
    }

    //UI - User chooses file
    public void sendFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to attach");
        File selectedFile = fileChooser.showOpenDialog(messageField.getScene().getWindow());

        if (selectedFile != null) {
            messageLabel.setText("File selected: " + selectedFile.getName());

            model.sendFile(selectedFile);
        } else {
            messageLabel.setText("No File Selected");
        }
    }



    }




