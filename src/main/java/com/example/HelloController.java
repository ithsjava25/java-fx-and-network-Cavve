package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
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

    @FXML
    private File attachedFile;

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

    public void sendFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to attach");
        File selectedFile = fileChooser.showOpenDialog(messageField.getScene().getWindow());


        if (selectedFile != null) {
//            attachedFile = selectedFile;
            messageLabel.setText("File selected: " + selectedFile.getName());
        } else {
            messageLabel.setText("No File Selected");
        }
    }

    }




