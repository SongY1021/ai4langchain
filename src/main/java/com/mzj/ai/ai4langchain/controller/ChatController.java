package com.mzj.ai.ai4langchain.controller;

import com.mzj.ai.ai4langchain.config.AgentConfiguration;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/ai")
public class    ChatController {
    private Logger logger = LoggerFactory.getLogger(ChatController.class);
    @Resource
    ChatLanguageModel chatLanguageModel;
    @Resource
    StreamingChatLanguageModel streamingChatLanguageModel;

    @Resource
    private AgentConfiguration.CloudAgent cloudAgent;


    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message", defaultValue = "Hello") String message) {
        return chatLanguageModel.chat(message);
    }

    @GetMapping(value = "/stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> stream(@RequestParam(value = "message", defaultValue = "你好") String message) {
        Flux<String> flux = Flux.create(fluxSink -> {
            streamingChatLanguageModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    fluxSink.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    fluxSink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    fluxSink.error(throwable);
                }
            });
        });
        return flux;
    }
    @GetMapping(value = "/stream2", produces = "text/stream;charset=UTF-8")
    public Flux<String> stream2(@RequestParam(value = "message", defaultValue = "你好") String message
            , @RequestParam("sessionId") String sessionId) {
        if (!StringUtils.hasLength(sessionId)) {
            sessionId = UUID.randomUUID().toString();
            logger.info("sessionId >>> {}", sessionId);
        }
        TokenStream stream = cloudAgent.stream(sessionId, message);
        Flux<String> flux = Flux.create(fluxSink -> {
            stream.onPartialResponse(partialResponse -> fluxSink.next(partialResponse))
                    .onCompleteResponse(completeResponse -> fluxSink.complete())
                    .onError(fluxSink::error)
                    .start();
        });
        return flux;
    }
}
