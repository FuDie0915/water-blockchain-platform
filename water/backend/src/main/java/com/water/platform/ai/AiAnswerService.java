package com.water.platform.ai;

import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiAnswerService {

    @SystemMessage(fromResource = "prompt/block-chain-answer.txt")
    Flux<String> chat(String message);
}
