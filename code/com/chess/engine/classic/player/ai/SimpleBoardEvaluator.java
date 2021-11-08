package engine.classic.player.ai;

import engine.classic.board.Board;
import engine.classic.pieces.Piece;
import engine.classic.player.Player;

public class SimpleBoardEvaluator
        implements BoardEvaluator {

    @Override
    public int evaluate(final Board board) {
        return scorePlayer(board.whitePlayer()) - scorePlayer(board.blackPlayer());
    }

    private static int scorePlayer(final Player player) {
        int score = player.getLegalMoves().size();
        for (final Piece piece : player.getActivePieces()) {
            score += (piece.getPieceValue() + piece.locationBonus());
        }
        if(player.getOpponent().isInCheckMate()) {
            score += 10000;
        }
        if(player.getPlayerKing().isCastled()) {
            score += 50;
        }
        return score;
    }

}
