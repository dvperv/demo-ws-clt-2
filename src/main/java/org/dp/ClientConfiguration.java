package org.dp;

import lombok.extern.slf4j.Slf4j;
import org.dp.model.OutputMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Base64;

@Slf4j
@Configuration
public class ClientConfiguration {
    @Bean
    WebSocketStompClient client(StompSessionHandler handler){
        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(
                new StandardWebSocketClient()
        );

        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String url = "ws://localhost:8080/stomp";

        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        String auth = "user1" + ":" + "password";
        webSocketHttpHeaders.add("Authorization", "Basic " + new String(Base64.getEncoder().encode(auth.getBytes())));

        webSocketStompClient.connectAsync(url, webSocketHttpHeaders, handler);

        return webSocketStompClient;
    }

    @Bean
    StompSessionHandler handler(){
        return new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                log.info("Subscribe...");
//                session.subscribe("/topic/messages", this);
                session.subscribe("/user/queue/messages", this);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.error("Got an exception", exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                log.error("Got a transport error", exception);
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return OutputMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                OutputMessage message = (OutputMessage) payload;
                log.info("Received : " + message.getText() + " from : " + message.getFrom() + " at " + message.getTime());
            }
        };
    }
}
