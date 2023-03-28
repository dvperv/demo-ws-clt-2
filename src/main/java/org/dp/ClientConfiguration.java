package org.dp;

import lombok.extern.slf4j.Slf4j;
import org.dp.model.OutputMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration
public class ClientConfiguration {
    @Bean
    public WebSocketStompClient client(StompSessionHandlerAdapter handler) {
        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(
                new StandardWebSocketClient()
        );
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        String auth = "user1" + ":" + "password";
        webSocketHttpHeaders.add("Authorization", "Basic " +
                Base64.getEncoder().encodeToString(auth.getBytes())
        );
        webSocketStompClient.connectAsync("ws://localhost:8080/stomp", webSocketHttpHeaders, handler);

        return webSocketStompClient;
    }

    @Bean
    public StompSessionHandlerAdapter handlerAdapter(){
        return new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return OutputMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                OutputMessage message = (OutputMessage) payload;
                log.info("Received : " + message.getText() + " from : " + message.getFrom() + " at " + message.getTime());
            }

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                log.info("Subscribe...");
//                session.subscribe("/topic/messages", this);
                session.subscribe("/user/queue", this);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.error("Got an exception", exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                log.error("Got a transport error", exception);
            }
        };
    }
}
