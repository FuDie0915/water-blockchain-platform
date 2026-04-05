package com.water.platform.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
class AiAnswerServiceTest {

    @Resource
    AiAnswerService aiAnswerService;


    @Test
    void testChat() {
        Flux<String> result = aiAnswerService.chat("区块链是什么");
        System.out.println(result);
    }
}