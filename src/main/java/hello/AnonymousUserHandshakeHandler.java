package hello;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class AnonymousUserHandshakeHandler extends DefaultHandshakeHandler{
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Principal principal = request.getPrincipal();
        if (principal == null) {
            principal = new Principal() {
                private String name;

                @Override
                public String getName() {
                    if (name == null) {
                        name = UUID.randomUUID().toString();
                    }
                    return name;
                }
            };
        }
        return principal;
    }
}
