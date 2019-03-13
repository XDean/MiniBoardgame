package xdean.mini.boardgame.server.mybatis.mapper;

import static xdean.mybatis.extension.SqlUtil.equal;
import static xdean.mybatis.extension.SqlUtil.wrapString;

import java.sql.Timestamp;

import org.apache.ibatis.session.RowBounds;

import xdean.mini.boardgame.server.model.entity.GamePlayerEntity;
import xdean.mini.boardgame.server.model.entity.GameRoomEntity;
import xdean.mini.boardgame.server.model.handler.GameBoardConverter;
import xdean.mini.boardgame.server.mybatis.Tables;
import xdean.mybatis.extension.MyBatisSQL;

public class GameMapperBuilder implements Tables {
  private GameBoardConverter gameBoardConverter = new GameBoardConverter();

  String findPlayer(int id) {
    return MyBatisSQL.create()
        .SELECT_FROM(GamePlayerTable.table)
        .WHERE(equal(GamePlayerTable.id.fullName, id))
        .toString();
  }

  String findRoom(int roomId) {
    return MyBatisSQL.create()
        .SELECT_FROM(GameRoomTable.table)
        .WHERE(equal(GameRoomTable.id.fullName, roomId))
        .toString();
  }

  String save(GamePlayerEntity e) {
    return MyBatisSQL.create()
        .INSERT_INTO(GamePlayerTable.table)
        .VALUES(GamePlayerTable.id.fullName, Integer.toString(e.getId()))
        .VALUES(GamePlayerTable.roomId.fullName, Integer.toString(e.getRoom().getId()))
        .VALUES(GamePlayerTable.seat.fullName, Integer.toString(e.getSeat()))
        .ON_DUPLICATE_KEY_UPDATE(GamePlayerTable.roomId, GamePlayerTable.seat)
        .toString();
  }

  String save(GameRoomEntity e) {
    return MyBatisSQL.create()
        .INSERT_INTO(GameRoomTable.table)
        .VALUES(GameRoomTable.id.fullName, Integer.toString(e.getId()))
        .VALUES(GameRoomTable.gameName.fullName, e.getGameName())
        .VALUES(GameRoomTable.roomName.fullName, e.getRoomName())
        .VALUES(GameRoomTable.createdTime.fullName, new Timestamp(e.getCreatedTime().getTime()).toString())
        .VALUES(GameRoomTable.playerCount.fullName, Integer.toString(e.getPlayerCount()))
        .VALUES(GameRoomTable.board.fullName, gameBoardConverter.toString(e.getBoard()))
        .ON_DUPLICATE_KEY_UPDATE(GameRoomTable.gameName, GameRoomTable.roomName, GameRoomTable.createdTime,
            GameRoomTable.playerCount, GameRoomTable.board)
        .toString();
  }

  String delete(int roomId) {
    return MyBatisSQL.create()
        .DELETE_FROM(GameRoomTable.table.name)
        .WHERE(equal(GameRoomTable.id.fullName, roomId))
        .toString();
  }

  String findAllRoom(String gameName, RowBounds page) {
    return MyBatisSQL.create()
        .SELECT_FROM(GameRoomTable.table)
        .WHERE(equal(GameRoomTable.gameName.fullName, wrapString(gameName)))
        .LIMIT(page.getLimit())
        .OFFSET(page.getOffset())
        .toString();
  }
}