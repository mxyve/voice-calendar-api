package top.xym.voice.calendar.app.model.dto;

import lombok.Data;

@Data
public class MessageSendRequest {
    private String content;
    private String audio;
}