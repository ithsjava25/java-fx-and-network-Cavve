package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WireMockTest
class HelloModelTest {

    @Test
    //information om vad det här testet gör - kommer ersätta metodnamnet i listan när det skrivs ut
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange - Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act - When
        model.sendMessage();
        //Assert - Then
        assertThat(spy.getMessage()).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRunTimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));

        model.sendMessage().join();

        //Verify call made to server
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(containing("Hello World")));

    }

    //skapar fake-connection (spy)
    //Testet ska verifiera att: När WireMock skickar tillbaka en JSON-rad med ett meddelande
    // så anropas messageHandler.accept med rätt data
    //WireMockRunTimeIfo -- startar en fake HTTP-server lokalt
    @Test
    @DisplayName("When Receiving JSON messages from fake server, then messageHandler should be called with correct content")
    void receiveMessageToFakeServer(WireMockRuntimeInfo wmRunTimeInfo) throws InterruptedException {

        // ------------------------ ARRANGE Given ------------------------
        //startar fake server
        var con = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());

        //Förbereder ett meddelande som ntfy ska skicka
        //typiskt meddelande som ntfy skickar tillbaka
        String jsonMessage = """
                {"id":"123","time":123456789,"event":"message","topic":"mytopic","message":"Hello from server"}
                """;

        //vi stubbar wiremock så att när någon gör GET till mytopic
        //så svarar servern med vår JSON-rad och content-type
        //Om någon gör GET till /mytopic/json, svara med status 200 och den här texten som kropp.
        stubFor(get("/mytopic/json")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(jsonMessage)
                ));


        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AssertionError> assertionErrorRef = new AtomicReference<>();
        AtomicBoolean wasCalled = new AtomicBoolean(false);

        // ------------------------ ACT When ------------------------
        //anropa dto och dto ska innehålla rätt data från JSON.et
        con.receive(dto -> {
            try {
                wasCalled.set(true);

                assertThat(dto.message()).isEqualTo("Hello from server");
                assertThat(dto.topic()).isEqualTo("mytopic");
            } catch (AssertionError e) {
                assertionErrorRef.set(e);
            } finally {
                latch.countDown();
            }
        });

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        // Vänta lite för att låta async-operationen hända


        // ------------------------ ASSERT Then ------------------------
        //verifiera att messageHandler verkligen anropades
        assertThat(completed)
                .as("messageHandler should have been called within timeout")
                .isTrue();

        if (assertionErrorRef.get() != null) {
            throw assertionErrorRef.get();
        }

        assertThat(wasCalled.get())
                .as("messageHandler should have been called with parsed message")
                .isTrue();

    }
}

