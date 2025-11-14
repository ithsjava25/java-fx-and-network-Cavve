package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.css.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRunTimeInfo){
        var con = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));

        model.sendMessage();

        //Verify call made to server
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(containing("Hello World")));

    }

    @Test
    void receiveMessagesFromFakeServer(WireMockRuntimeInfo wmRunTimeInfo) throws InterruptedException {
        // ------------------------ ARRANGE Given ------------------------
        //startar fake server
        var con = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());
        var model = new HelloModel(con);

        String jsonMessage = """
            {"id":"123","time":123456789,"event":"message","topic":"mytopic","message":"Hello from server"}
            """;

        stubFor(get(urlEqualTo("/mytopic/json"))
                .willReturn(okForContentType("application/json", jsonMessage)));

        model.receiveMessage();

        Thread.sleep(500);
        assertThat(model.getMessages().size()).isEqualTo(1);
        var dto = model.getMessages().getFirst();
        assertThat(dto.message()).isEqualTo("Hello from server");

        verify(getRequestedFor(urlEqualTo("/mytopic/json")));
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

        //skapar egen flagga för att kolla om messageHandler anropas
        final boolean[] wasCalled = {false};

        // ------------------------ ACT When ------------------------
        //anropa dto och dto ska innehålla rätt data från JSON.et
        con.receive(dto -> {
            wasCalled[0] = true;
            assertThat(dto.message()).isEqualTo("Hello from server");
            assertThat(dto.topic()).isEqualTo("mytopic");
        });

        // Vänta lite för att låta async-operationen hända
        Thread.sleep(500);

        // ------------------------ ASSERT Then ------------------------
        //verifiera att messageHandler verkligen anropades
        assertThat(wasCalled[0])
                .as("messageHandler should have been called with parsed message")
                .isTrue();
    }

}

