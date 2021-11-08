package engine.player.ai;

import engine.board.Board;
import engine.board.Move;

public interface MoveStrategy {

    public String getName();

    public long getNumBoardsEvaluated();

    public Move execute(Board board);

}
