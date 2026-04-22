package com.water.platform.api;

import cn.dev33.satoken.stp.StpUtil;
import com.water.platform.ai.AiAnswerService;
import com.water.platform.base.common.ErrorCode;
import com.water.platform.base.exception.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
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
    public Flux<ServerSentEvent<String>> chat(String message, String satoken, HttpServletRequest request) {
        // SSE无法设置Header，需要同时支持Header和URL参数两种方式获取token
        String token = request.getHeader("satoken");
        if (StringUtils.isBlank(token)) {
            // 从URL参数获取token
            token = satoken;
        }
        ThrowUtils.throwIf(StringUtils.isBlank(token), ErrorCode.HEADER_PARAMS_ERROR);
        Object loginId = StpUtil.getLoginIdByToken(token);
        ThrowUtils.throwIf(loginId == null, ErrorCode.NOT_LOGIN_ERROR);
        return aiAnswerService.chat(message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
