package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import rich.RichHit;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import javax.websocket.*;

@ClientEndpoint
public class WebSocketSink implements SinkFunction<RichHit> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Session session;
    private static CountDownLatch latch = new CountDownLatch(1);

    public WebSocketSink() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("ws://localhost:8083/hits"));
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        WebSocketSink.session = session;
        latch.countDown();
    }

    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void invoke(RichHit value, Context context) {
        try {
            if (session != null && session.isOpen()) {
                String json = objectMapper.writeValueAsString(value);
                session.getAsyncRemote().sendText(json);
            } else {
                System.err.println("⚠️ WebSocket session is closed. Data not sent.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
