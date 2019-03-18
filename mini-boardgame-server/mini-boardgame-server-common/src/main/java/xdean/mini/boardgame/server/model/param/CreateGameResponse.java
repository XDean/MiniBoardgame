package xdean.mini.boardgame.server.model.param;

import lombok.Builder;
import lombok.Value;
import lombok.Builder.Default;

@Value
@Builder
public class CreateGameResponse {
  @Default
  int roomId = -1;
}
