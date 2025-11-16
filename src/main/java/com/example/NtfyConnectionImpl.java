package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean send(String message) {
        //villkor för felhantering
        if (message == null || message.isBlank()) {
            System.out.println("Error: message is null or blank");
            return false;
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .build();
        try {
            //Todo: handle long blocking send requests to not freeze the JavaFX thread
            //1. Use thread send message?
            //2. Use async?
            var reponse = http.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
            System.out.println("Interruped sending message");
        }
        return false;
    }

    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            System.out.println("Error: file is null");
            return false;
        }

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Filename", file.getName())
                    .build();

            http.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            return true;

        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending file: " + e.getMessage());
            return false;
        }
    }


    @Override
    //använd stub för att skapa fake server och se om vi tar emot något
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        if (messageHandler == null) {
            throw new IllegalArgumentException("messageHandler cannot be null");
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response ->
                        response.body()
                                .map(s -> {
                                    try {
                                        return mapper.readValue(s, NtfyMessageDto.class);
                                    } catch (Exception e) {
                                        System.out.println("Error parsing message: " + e.getMessage());
                                        throw e;
                                    }
                                })
                                .filter(message -> "message".equals(message.event()))
                                .peek(System.out::println)
                                .forEach(messageHandler)
                )
                .exceptionally(ex -> {
                    System.out.println("Error receiving messages: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }
}