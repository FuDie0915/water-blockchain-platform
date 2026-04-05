package com.water.platform.api;

import com.water.platform.ai.AiAnswerService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiApi {

    @Resource
    private AiAnswerService aiAnswerService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(String message) {
        return aiAnswerService.chat(message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
