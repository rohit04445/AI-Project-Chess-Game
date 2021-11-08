package com.chess.engine.classic.player.ai;

import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.Move;

public interface MoveStrategy {

    public String getName();

    public long getNumBoardsEvaluated();

    public Move execute(Board board);

}
