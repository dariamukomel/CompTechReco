package web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerEndpointConfig;

public class WebSocketServerMain {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8083);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServerContainer wsContainer = WebSocketServerContainerInitializer.configureContext(context);

        wsContainer.addEndpoint(
                ServerEndpointConfig.Builder
                        .create(WebSocketServer.class, "/hits")
                        .build()
        );

        server.start();
        server.join();
    }
}
