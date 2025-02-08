package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import rich.RichHit;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/hits")
public class WebSocketServer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("✅ New connection opened: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            RichHit hit = objectMapper.readValue(message, RichHit.class);
            broadcast(hit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    public static void broadcast(RichHit hit) {
        try {
            String message = objectMapper.writeValueAsString(hit);

            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                } else {
                    sessions.remove(session);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

