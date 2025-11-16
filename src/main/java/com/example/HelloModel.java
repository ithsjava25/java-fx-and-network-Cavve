package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;

    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public String getMessageToSend() {
        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    public CompletableFuture<Boolean> sendMessage() {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }
        return connection.send(msg);
    }

    static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            // JavaFX toolkit not initialized (e.g., unit tests): run inline
            task.run();
        }
    }

    public void receiveMessage() {
        connection.receive(m -> runOnFx(() -> messages.add(m)));
    }

    public CompletableFuture<Boolean> sendFile (File file) {
        return connection.sendFile(file);
    }

}