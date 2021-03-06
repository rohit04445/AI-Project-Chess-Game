package engine.player.ai;

import engine.board.Board;
import engine.pieces.Piece;
import engine.player.Player;

public class BoardEvaluator {

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
