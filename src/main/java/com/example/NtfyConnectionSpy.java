package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;

    @Override
    public boolean send(String message) {
        this.message = message;
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {

    }
}

/*
testdubb som låtsas vara riktiga NtfyConnection, men utan att prata med nätet
Observerar vad som händer utan att påverka
 */
