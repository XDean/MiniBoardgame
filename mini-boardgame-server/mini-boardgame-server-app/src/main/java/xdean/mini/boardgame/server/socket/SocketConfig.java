package xdean.mini.boardgame.server.socket;

import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.google.common.collect.ImmutableList;

import xdean.mini.boardgame.server.model.GameConstants;

@Configuration
@EnableWebSocket
public class SocketConfig implements WebSocketConfigurer, GameConstants {
  @Inject
  TimeSocketHandler handler;

  @Inject
  GameSocketHandler gameHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(handler, "/socket-test/**")
        .addHandler(gameHandler, "/game/room").addInterceptors(new HttpSessionHandshakeInterceptor(ImmutableList.of(ROOM)))
        .setAllowedOrigins("*");
  }
}