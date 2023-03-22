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
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

@Slf4j
@Configuration
public class ClientConfiguration {
    private final String url = "ws://localhost:8080/chat";

    @Bean
    WebSocketStompClient client(StompSessionHandler handler){
        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(
                new StandardWebSocketClient()
        );

        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
        webSocketStompClient.connectAsync(url, handler);

        return webSocketStompClient;
    }

    @Bean
    StompSessionHandler handler(){
        return new StompSessionHandler() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                log.info("Subscribe to /chat");
                session.subscribe("/topic/messages", this);
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
