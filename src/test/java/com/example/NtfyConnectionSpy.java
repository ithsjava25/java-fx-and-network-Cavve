package com.example;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    private String message;
    private File sentFile;

    public String getMessage(){
        return message;
    }

    public File getSentFile(){
        return sentFile;
    }
    
    @Override
    public CompletableFuture<Boolean> send(String message) {
        this.message = message;
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {

    }

    @Override
    public CompletableFuture<Boolean> sendFile(File file) {
        this.sentFile = file;
        if (file == null || !file.exists()) {
            System.out.println("File does not exist");
        }
        return CompletableFuture.completedFuture(false);
    }
}

/*
testdubb som låtsas vara riktiga NtfyConnection, men utan att prata med nätet
Observerar vad som händer utan att påverka
 */
