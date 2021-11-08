package engine.classic.player.ai;

import engine.classic.board.Board;
import engine.classic.board.Move;

public interface MoveStrategy {

    public String getName();

    public long getNumBoardsEvaluated();

    public Move execute(Board board);

}
