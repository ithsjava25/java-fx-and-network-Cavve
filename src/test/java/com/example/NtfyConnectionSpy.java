package com.example;

import java.io.File;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;
    File sentFile;

    @Override
    public boolean send(String message) {
        this.message = message;
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {

    }

    @Override
    public boolean sendFile(File file) {
        this.sentFile = file;
        if (file == null || !file.exists()) {
            System.out.println("File does not exist");
        }
        return false;
    }

    @Override
    public boolean downloadFile(String topic, String fileName, File destination) {
        return false;
    }
}

/*
testdubb som låtsas vara riktiga NtfyConnection, men utan att prata med nätet
Observerar vad som händer utan att påverka
 */
