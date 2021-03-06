package xdean.mini.boardgame.server.mybatis.mapper;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import xdean.mini.boardgame.server.model.entity.GamePlayerEntity;
import xdean.mini.boardgame.server.model.entity.GameRoomEntity;
import xdean.mini.boardgame.server.model.handler.ObjectJsonConverter;
import xdean.mini.boardgame.server.mybatis.Tables;
import xdean.mybatis.extension.resultmap.InitResultMap;

@Configuration
public class GameResultMap implements Tables {

  @Inject
  UserResultMap userResultMap;

  @Bean
  public InitResultMap<GameRoomEntity> gameRoomMapper() {
    return InitResultMap.create(GameRoomEntity.class)
        .namespaceIdByType()
        .resultMap(b -> b
            .stringFree()
            .mapping(GameRoomTable.id, GameRoomEntity::setId)
            .mapping(GameRoomTable.gameName, GameRoomEntity::setGameName)
            .mapping(GameRoomTable.roomName, GameRoomEntity::setRoomName)
            .mapping(GameRoomTable.createdTime, GameRoomEntity::setCreatedTime)
            .mapping(GameRoomTable.playerCount, GameRoomEntity::setPlayerCount)
            .mapping(d -> d.column(GameRoomTable.config)
                .property(GameRoomEntity::setConfig)
                .typeHandler(ObjectJsonConverter.class))
            .mapping(d -> d.column(GameRoomTable.board)
                .property(GameRoomEntity::setBoard)
                .typeHandler(ObjectJsonConverter.class)))
        .build();
  }

  @Bean
  public InitResultMap<GamePlayerEntity> gamePlayerMapper() {
    return InitResultMap.create(GamePlayerEntity.class)
        .namespaceIdByType()
        .resultMap(b -> b
            .stringFree()
            .mapping(GamePlayerTable.id, GamePlayerEntity::setId)
            .mapping(GamePlayerTable.seat, GamePlayerEntity::setSeat)
            .mapping(GamePlayerTable.ready, GamePlayerEntity::isReady)
            .mapping(d -> d.property(GamePlayerEntity::setProfile).nestMap(userResultMap.userProfileMapper().getId())))
        .build();
  }
}
