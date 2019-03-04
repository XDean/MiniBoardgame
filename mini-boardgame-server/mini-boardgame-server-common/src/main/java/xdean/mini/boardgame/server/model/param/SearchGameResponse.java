package xdean.mini.boardgame.server.model.param;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.Builder.Default;
import xdean.mini.boardgame.server.model.GameRoom;

@Value
@Builder
public class SearchGameResponse {
  @Default
  int errorCode = 0;

  @Singular
  List<GameRoom> rooms;
}