package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javafx.geometry.Insets;

import java.awt.*;
import java.io.File;
import java.net.URI;
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
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                VBox container = new VBox(4);

                Label topicLabel = new Label(item.topic());
                topicLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #F48C2A;");

                Label messageLabel = new Label(item.message());
                messageLabel.setWrapText(true);

                container.getChildren().addAll(topicLabel, messageLabel);

                // Om fil finns, lägg till Hyperlink
                if (item.attachmentUrl() != null && !item.attachmentUrl().isEmpty()) {
                    Hyperlink downloadLink = new Hyperlink("Download File");
                    downloadLink.setOnAction(e -> {
                        try {
                            Desktop.getDesktop().browse(new URI(item.attachmentUrl()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error opening link");
                            alert.setContentText("Could not open attachment: " + ex.getMessage());
                            alert.showAndWait();
                        }
                    });
                    container.getChildren().add(downloadLink);
                }

                container.setPadding(new Insets(8));
                container.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 6;");

                setGraphic(container);
                setText(null); // text används ej
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




