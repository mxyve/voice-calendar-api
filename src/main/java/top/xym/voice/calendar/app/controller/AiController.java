package top.xym.voice.calendar.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.xym.voice.calendar.app.common.result.Result;
import top.xym.voice.calendar.app.model.dto.MessageSendRequest;
import top.xym.voice.calendar.app.service.MessageService;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final MessageService messageService;

    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody MessageSendRequest request) {
        return Result.ok(messageService.sendMessage(request));
    }
}