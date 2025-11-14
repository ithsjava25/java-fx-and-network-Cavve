package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//error om det kommer något okänt fält
@JsonIgnoreProperties(ignoreUnknown = true)
//dto - data transfer object.
public record NtfyMessageDto(String id, long time, String event, String topic, String message, attachmentDto attachment, String fileName) {

    public String attachmentUrl() {
        return attachment != null ? attachment.url() : null;
    }

}
