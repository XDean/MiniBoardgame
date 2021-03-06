package xdean.mini.boardgame.server.mybatis.mapper;

import static xdean.mybatis.extension.SqlUtil.equal;
import static xdean.mybatis.extension.SqlUtil.together;
import static xdean.mybatis.extension.SqlUtil.wrapString;

import java.sql.Timestamp;

import xdean.mini.boardgame.server.model.entity.GamePlayerEntity;
import xdean.mini.boardgame.server.model.entity.GameRoomEntity;
import xdean.mini.boardgame.server.model.handler.ObjectJsonConverter;
import xdean.mini.boardgame.server.model.param.SearchGameRequest;
import xdean.mini.boardgame.server.mybatis.Tables;
import xdean.mybatis.extension.MyBatisSQL;
import xdean.mybatis.extension.MyBatisSQL.MSB;

public class GameMapperBuilder extends BaseMapperBuilder implements Tables {
  private ObjectJsonConverter objectConverter = new ObjectJsonConverter();

  public String findPlayer(Integer id) {
    return MyBatisSQL.create()
        .SELECT_FROM(GamePlayerTable.table)
        .SELECT_ALL(ProfileTable.table)
        .INNER_JOIN(ProfileTable.table, equal(GamePlayerTable.id, ProfileTable.id))
        .WHERE(equal(GamePlayerTable.id.fullName, id))
        .toString();
  }

  public String findRoom(Integer roomId) {
    return MyBatisSQL.create()
        .SELECT_FROM(GameRoomTable.table)
        .WHERE(equal(GameRoomTable.id.fullName, roomId))
        .toString();
  }

  public String findRoomByPlayer(Integer playerId) {
    return MyBatisSQL.create()
        .SELECT_FROM(GameRoomTable.table)
        .INNER_JOIN(GamePlayerTable.table, equal(GamePlayerTable.roomId, GameRoomTable.id))
        .WHERE(equal(GamePlayerTable.id.fullName, playerId))
        .toString();
  }

  public String findAllPlayersInRoom(Integer roomId) {
    return MyBatisSQL.create()
        .SELECT_FROM(GamePlayerTable.table)
        .SELECT_ALL(ProfileTable.table)
        .INNER_JOIN(ProfileTable.table, equal(GamePlayerTable.id, ProfileTable.id))
        .WHERE(equal(GamePlayerTable.roomId.fullName, roomId))
        .toString();
  }

  public String savePlayer(GamePlayerEntity e) {
    if (e.getRoom().isPresent()) {
      return MyBatisSQL.create()
          .INSERT_INTO(GamePlayerTable.table)
          .VALUES(GamePlayerTable.id.fullName, Integer.toString(e.getId()))
          .VALUES(GamePlayerTable.roomId.fullName, Integer.toString(e.getRoom().get().getId()))
          .VALUES(GamePlayerTable.seat.fullName, Integer.toString(e.getSeat()))
          .VALUES(GamePlayerTable.ready.fullName, Boolean.toString(e.isReady()))
          .ON_DUPLICATE_KEY_UPDATE(GamePlayerTable.roomId, GamePlayerTable.seat, GamePlayerTable.ready)
          .toString();
    } else {
      return MyBatisSQL.create()
          .DELETE_FROM(GamePlayerTable.table.name)
          .WHERE(equal(GamePlayerTable.id.fullName, e.getId()))
          .toString();
    }
  }

  public String saveRoom(GameRoomEntity e) {
    return MyBatisSQL.create()
        .INSERT_INTO(GameRoomTable.table)
        .VALUES(GameRoomTable.id.fullName, Integer.toString(e.getId()))
        .VALUES(GameRoomTable.gameName.fullName, wrapString(e.getGameName()))
        .VALUES(GameRoomTable.roomName.fullName, wrapString(e.getRoomName()))
        .VALUES(GameRoomTable.createdTime.fullName, wrapString(new Timestamp(e.getCreatedTime().getTime()).toString()))
        .VALUES(GameRoomTable.playerCount.fullName, Integer.toString(e.getPlayerCount()))
        .VALUES(GameRoomTable.config.fullName, wrapString(objectConverter.toString(e.getConfig())))
        .VALUES(GameRoomTable.board.fullName, wrapString(objectConverter.toString(e.getBoard())))
        .ON_DUPLICATE_KEY_UPDATE(GameRoomTable.gameName, GameRoomTable.roomName, GameRoomTable.createdTime,
            GameRoomTable.playerCount, GameRoomTable.config, GameRoomTable.board)
        .toString();
  }

  public String delete(Integer roomId) {
    return together(
        MyBatisSQL.create()
            .DELETE_FROM(GamePlayerTable.table.name)
            .WHERE(equal(GamePlayerTable.roomId.fullName, roomId))
            .toString(),
        MyBatisSQL.create()
            .DELETE_FROM(GameRoomTable.table.name)
            .WHERE(equal(GameRoomTable.id.fullName, roomId))
            .toString());
  }

  public String searchRoom(SearchGameRequest request) {
    return MyBatisSQL.create()
        .SELECT_FROM(GameRoomTable.table)
        .COMPOSE(MSB.when(!request.getGameName().isEmpty(), sql -> sql
            .WHERE(equal(GameRoomTable.gameName.fullName, wrapString(request.getGameName())))))
        .COMPOSE(MSB.when(request.getRoomId() != -1, sql -> sql
            .WHERE(equal(GameRoomTable.id.fullName, Integer.toString(request.getRoomId())))))
        .OFFSET(request.getPage() * request.getPageSize())
        .LIMIT(request.getPageSize())
        .toString();
  }
}
