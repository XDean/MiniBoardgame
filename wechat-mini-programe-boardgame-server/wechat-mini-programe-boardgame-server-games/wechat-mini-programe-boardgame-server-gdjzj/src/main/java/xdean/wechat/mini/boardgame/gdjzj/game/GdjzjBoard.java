package xdean.wechat.mini.boardgame.gdjzj.game;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import xdean.jex.extra.collection.IntList;
import xdean.wechat.mini.boardgame.gdjzj.model.GdjzjErrorCode;
import xdean.wechat.mini.boardgame.server.model.Player;
import xdean.wechat.mini.boardgame.server.model.exception.MiniBoardgameException;

public class GdjzjBoard {
  ImmutableList<GdjzjCard> cards;
  ImmutableList<GdjzjPlayer> players;

  IntegerProperty currentPlayer = new SimpleIntegerProperty(this, "currentPlayer");
  IntegerProperty currentTurn = new SimpleIntegerProperty(this, "currentTurn");

  public GdjzjBoard(Player[] players) {
    this.cards = createCards();
    List<GdjzjRole> roles = GdjzjRole.getRoles(players.length);
    this.players = ImmutableList.copyOf(IntStream.range(0, players.length)
        .mapToObj(i -> new GdjzjPlayer(players[i], i, this, roles.get(i)))
        .collect(Collectors.toList()));
  }

  public void nextTurn() {
    int turn = currentTurn.get();
    if (turn < 2) {
      this.currentTurn.set(turn + 1);
    }
  }

  public GdjzjVoteCardResult getVoteCardResult(int turn) {
    assertTurn(turn);
    List<Integer> result = players.stream()
        .map(p -> p.turnInfos[turn])
        .filter(t -> t.vote)
        .flatMapToInt(t -> IntStream.of(t.vote1, t.vote2))
        .filter(i -> i != -1)
        .boxed()
        .collect(Collectors.groupingBy(t -> t))
        .entrySet()
        .stream()
        .sorted(Comparator.<Entry<Integer, List<Integer>>, Integer> comparing(e -> -e.getValue().size())
            .thenComparing(e -> e.getKey()))
        .map(e -> e.getKey())
        .limit(2L)
        .collect(Collectors.toList());
    return new GdjzjVoteCardResult(result.size() > 0 ? result.get(0) : -1, result.size() > 1 ? result.get(1) : -1);
  }

  public IntList getLeftPlayers() {
    int turn = currentTurn.get();
    return IntList.create(players.stream().filter(p -> p.turnInfos[turn].order < 0).mapToInt(p -> p.index).toArray());
  }

  public Optional<GdjzjPlayer> getPlayer(GdjzjRole role) {
    return players.stream().filter(p -> p.role == role).findFirst();
  }

  private ImmutableList<GdjzjCard> createCards() {
    return ImmutableList.copyOf(IntStream.range(0, 12)
        .mapToObj(i -> new GdjzjCard(i, i / 2 == 0))
        .sorted(Comparator.comparing(c -> c.index % 4 + Math.random()))
        .collect(Collectors.toList()));
  }

  public static void assertTurn(int turn) {
    if (turn < 0 || turn > 2) {
      throw MiniBoardgameException.builder()
          .code(GdjzjErrorCode.ILLEGAL_TURN)
          .build();
    }
  }
}
