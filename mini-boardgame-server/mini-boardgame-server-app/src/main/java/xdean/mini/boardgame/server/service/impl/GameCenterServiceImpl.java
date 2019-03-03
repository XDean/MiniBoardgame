package xdean.mini.boardgame.server.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xdean.mini.boardgame.server.model.GamePlayer;
import xdean.mini.boardgame.server.model.GameRoom;
import xdean.mini.boardgame.server.model.param.CreateGameRequest;
import xdean.mini.boardgame.server.model.param.CreateGameResponse;
import xdean.mini.boardgame.server.model.param.ExitGameRequest;
import xdean.mini.boardgame.server.model.param.ExitGameResponse;
import xdean.mini.boardgame.server.model.param.GameCenterErrorCode;
import xdean.mini.boardgame.server.model.param.JoinGameRequest;
import xdean.mini.boardgame.server.model.param.JoinGameResponse;
import xdean.mini.boardgame.server.service.GameCenterService;
import xdean.mini.boardgame.server.service.GameService;

@Service
public class GameCenterServiceImpl implements GameCenterService {

  @Autowired(required = false)
  List<GameService> games = Collections.emptyList();

  Map<Integer, GameRoom> rooms = new ConcurrentHashMap<>();

  @Override
  public CreateGameResponse createGame(GamePlayer player, CreateGameRequest request) {
    Optional<GameService> game = findGame(request.getGameName());
    if (!game.isPresent()) {
      return CreateGameResponse.builder()
          .errorCode(GameCenterErrorCode.NO_SUCH_GAME)
          .build();
    }
    synchronized (player) {
      if (player.getRoomId() != -1) {
        return CreateGameResponse.builder()
            .errorCode(GameCenterErrorCode.ALREADY_IN_ROOM)
            .build();
      }
      Integer roomId = generateId();
      GameRoom room = new GameRoom(roomId, request.getPlayerCount());
      room.addPlayer(player);
      rooms.put(roomId, room);
      player.setRoomId(roomId);
      return CreateGameResponse.builder()
          .roomId(roomId)
          .build();
    }
  }

  @Override
  public JoinGameResponse joinGame(GamePlayer player, JoinGameRequest request) {
    GameRoom room = rooms.get(request.getRoomId());
    if (room == null) {
      return JoinGameResponse.builder()
          .errorCode(GameCenterErrorCode.NO_SUCH_ROOM)
          .build();
    }
    synchronized (player) {
      if (player.getRoomId() != -1) {
        return JoinGameResponse.builder()
            .errorCode(GameCenterErrorCode.ALREADY_IN_ROOM)
            .build();
      }
      room.addPlayer(player);
      player.setRoomId(room.id);
      return JoinGameResponse.builder()
          .build();
    }
  }

  @Override
  public ExitGameResponse exitGame(GamePlayer player, ExitGameRequest request) {
    return null;
//    GameRoom room = rooms.get(request.getRoomId());
//    if (room == null) {
//      return ExitGameResponse.builder()
//          .errorCode(GameCenterErrorCode.NO_SUCH_ROOM)
//          .build();
//    }
//    synchronized (player) {
//      if (player.getRoomId() == -1) {
//        return ExitGameResponse.builder()
//            .errorCode(GameCenterErrorCode.NOT_IN_ROOM)
//            .build();
//      }
//      room.removePlayer(player);
//      player.setRoomId(-1);
//      if (room.isEmpty()) {
//        rooms.remove(room.id, room);
//      }
//      return ExitGameResponse.builder()
//          .build();
//    }
  }

  private Integer generateId() {
    Random r = new Random();
    Integer id;
    do {
      id = r.nextInt(10000);
    } while (rooms.keySet().contains(id));
    return id;
  }

  private Optional<GameService> findGame(String name) {
    return games.stream().filter(g -> g.name().equals(name)).findFirst();
  }
}
