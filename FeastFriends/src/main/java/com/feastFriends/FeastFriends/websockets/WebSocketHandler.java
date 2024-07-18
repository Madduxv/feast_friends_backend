package com.feastFriends.feastFriends.websockets;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.*;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    // Map to store messages for each session
    private final Map<WebSocketSession, List<String>> sessionMessages = new HashMap<>();
    private final Map<WebSocketSession, List<String>> requestedGenres = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // Initialize an empty message list for the new session
        sessionMessages.put(session, new ArrayList<>());
        requestedGenres.put(session, new ArrayList<>());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        List<String> messages = sessionMessages.get(session);
        if (messages != null) {
            // Add the received message to the session's message list
            messages.add(message.getPayload());

            // Optionally, you can broadcast the message to other sessions or process it
            broadcastToGroup(session, message);
        }
    }

    private void broadcastToGroup(WebSocketSession senderSession, TextMessage message) {
        // Example method to broadcast to a group, if needed
        // Implement logic to broadcast message to a group of sessions
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // Clean up the message list when the session is closed
        sessionMessages.remove(session);
    }

    // Method to get stored messages for a session
    public List<String> getMessagesForSession(WebSocketSession session) {
        return sessionMessages.getOrDefault(session, new ArrayList<>());
    }

    public List<String> getRequestedGenres(WebSocketSession session) {
        return sessionMessages.getOrDefault(session, new ArrayList<>());
    }

}
