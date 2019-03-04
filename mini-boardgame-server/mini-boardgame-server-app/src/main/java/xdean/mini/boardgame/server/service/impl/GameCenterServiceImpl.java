package xdean.mini.boardgame.server.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xdean.mini.boardgame.server.model.entity.GamePlayerEntity;
import xdean.mini.boardgame.server.model.entity.GameRoomEntity;
import xdean.mini.boardgame.server.model.entity.UserEntity;
import xdean.mini.boardgame.server.model.param.CreateGameRequest;
import xdean.mini.boardgame.server.model.param.CreateGameResponse;
import xdean.mini.boardgame.server.model.param.ExitGameRequest;
import xdean.mini.boardgame.server.model.param.ExitGameResponse;
import xdean.mini.boardgame.server.model.param.GameCenterErrorCode;
import xdean.mini.boardgame.server.model.param.JoinGameRequest;
import xdean.mini.boardgame.server.model.param.JoinGameResponse;
import xdean.mini.boardgame.server.service.GameCenterService;
import xdean.mini.boardgame.server.service.GamePlayerRepo;
import xdean.mini.boardgame.server.service.GameRoomRepo;
import xdean.mini.boardgame.server.service.GameService;
import xdean.mini.boardgame.server.service.UserService;
import xdean.mini.boardgame.server.util.JpaUtil;

@Service
public class GameCenterServiceImpl implements GameCenterService {

  @Autowired(required = false)
  List<GameService> games = Collections.emptyList();

  private @Inject UserService userService;
  private @Inject GamePlayerRepo gamePlayerRepo;
  private @Inject GameRoomRepo gameRoomRepo;

  private final Object[] locks = IntStream.range(0, 32).mapToObj(i -> new Object()).toArray();

  @Override
  public CreateGameResponse createGame(CreateGameRequest request) {
    Optional<GameService> game = findGame(request.getGameName());
    if (!game.isPresent()) {
      return CreateGameResponse.builder()
          .errorCode(GameCenterErrorCode.NO_SUCH_GAME)
          .build();
    }
    Optional<UserEntity> user = userService.getCurrentUser();
    if (!user.isPresent()) {
      return CreateGameResponse.builder()
          .errorCode(GameCenterErrorCode.NO_USER)
          .build();
    }
    UserEntity e = user.get();
    synchronized (getLock(e.getId())) {
      GamePlayerEntity player = JpaUtil.findOrCreate(gamePlayerRepo, e.getId(),
          id -> GamePlayerEntity.builder().userId(id).build());
      if (player.getRoom().getId() != -1) {
        return CreateGameResponse.builder()
            .errorCode(GameCenterErrorCode.ALREADY_IN_ROOM)
            .build();
      }
      Integer roomId = generateId();
      GameRoomEntity room = GameRoomEntity.builder()
          .id(roomId)
          .player(player)
          .build();
      room = gameRoomRepo.save(room);
      player.setRoom(room);
      return CreateGameResponse.builder()
          .roomId(roomId)
          .build();
    }
  }

  @Override
  public JoinGameResponse joinGame(JoinGameRequest request) {
    Optional<UserEntity> user = userService.getCurrentUser();
    if (!user.isPresent()) {
      return JoinGameResponse.builder()
          .errorCode(GameCenterErrorCode.NO_USER)
          .build();
    }
    synchronized (getLock(user.get().getId())) {
      synchronized (getLock(request.getRoomId())) {
        Optional<GameRoomEntity> oRoom = gameRoomRepo.findById(request.getRoomId());
        if (!oRoom.isPresent()) {
          return JoinGameResponse.builder()
              .errorCode(GameCenterErrorCode.NO_SUCH_ROOM)
              .build();
        }
        GameRoomEntity room = oRoom.get();
        GamePlayerEntity player = JpaUtil.findOrCreate(gamePlayerRepo, user.get().getId(),
            id -> GamePlayerEntity.builder().userId(id).build());
        if (player.getRoom().getId() != -1) {
          return JoinGameResponse.builder()
              .errorCode(GameCenterErrorCode.ALREADY_IN_ROOM)
              .build();
        }
        room.getPlayers().add(player);
        player.setRoom(room);
        gameRoomRepo.save(room);
        // gamePlayerRepo.save(player);
        return JoinGameResponse.builder()
            .build();
      }
    }
  }

  @Override
  public ExitGameResponse exitGame(ExitGameRequest request) {
    Optional<UserEntity> user = userService.getCurrentUser();
    if (!user.isPresent()) {
      return ExitGameResponse.builder()
          .errorCode(GameCenterErrorCode.NO_USER)
          .build();
    }
    synchronized (getLock(user.get().getId())) {
      GamePlayerEntity player = JpaUtil.findOrCreate(gamePlayerRepo, user.get().getId(),
          id -> GamePlayerEntity.builder().userId(id).build());
      synchronized (getLock(player.getRoom().getId())) {
        if (player.getRoom().getId() == -1) {
          return ExitGameResponse.builder()
              .errorCode(GameCenterErrorCode.NOT_IN_ROOM)
              .build();
        }
        player.setRoom(null);
        gamePlayerRepo.save(player);
        return ExitGameResponse.builder().build();
      }
    }
  }

  private Integer generateId() {
    Random r = new Random();
    Integer id;
    do {
      id = r.nextInt(10000);
    } while (gameRoomRepo.existsById(id));
    return id;
  }

  private Optional<GameService> findGame(String name) {
    return games.stream().filter(g -> g.name().equals(name)).findFirst();
  }

  private Object getLock(int id) {
    return locks[id / 32];
  }
}
