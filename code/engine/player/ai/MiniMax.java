package engine.player.ai;

import engine.Alliance;
import engine.board.Board;
import engine.board.Move;
import engine.board.Board.MoveStatus;
import engine.player.Player;

public class MiniMax implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private long boardsEvaluated;
    private long executionTime;
    int depth;

    public MiniMax(int depth) {
        this.evaluator = new SimpleBoardEvaluator();
        this.boardsEvaluated = 0;
        this.depth=depth;
    }

    @Override
    public String getName() {
        return "MiniMax";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        final Player currentPlayer = board.currentPlayer();
        final Alliance alliance = currentPlayer.getAlliance();
        Move best_move = null;
        int highest_seen_value = Integer.MIN_VALUE;
        int lowest_seen_value = Integer.MAX_VALUE;
        int current_value;
        System.out.println(board.currentPlayer() + " THINKING with depth = " +depth);
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final Board imaginary = board.createCopy();
            final MoveStatus status = Player.makeMove(imaginary, move);
            if (status == MoveStatus.DONE) {
                if(alliance.isWhite()){
                    current_value =min(imaginary, depth - 1);
                    System.out.println("\t" + getName() + " move " + move + " scores " + current_value);
                    if(current_value >= highest_seen_value) {
                        highest_seen_value = current_value;
                        best_move = move;
                }
            }
                else{
                    current_value =  max(imaginary, depth - 1);
                    System.out.println("\t" + getName() + " move " + move + " scores " + current_value);
                    if(current_value <= lowest_seen_value) {
                        lowest_seen_value = current_value;
                        best_move = move;
                }
               
            }
        }
    }
        this.executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("%s SELECTS %s [#boards = %d time taken = %d ms, rate = %.1f\n", board.currentPlayer(),
                best_move, this.boardsEvaluated, this.executionTime, (1000 * ((double)this.boardsEvaluated/this.executionTime)));
        return best_move;
    }

    public int min(final Board board, final int depth) {
        if (depth == 0 ||
                board.currentPlayer().isInCheckMate() ||
                board.currentPlayer().getOpponent().isInCheckMate()) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board);
        }
        int lowest_seen_value = Integer.MAX_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final Board imaginary = board.createCopy();
            final MoveStatus status = Player.makeMove(imaginary, move);
            if (status == MoveStatus.DONE) {
                final int current_value = max(imaginary, depth - 1);
                if (current_value <= lowest_seen_value) {
                    lowest_seen_value = current_value;
                }
            }
        }
        return lowest_seen_value;
    }

    public int max(final Board board, final int depth) {
        if (depth == 0 ||
                board.currentPlayer().isInCheckMate() ||
                board.currentPlayer().getOpponent().isInCheckMate()) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board);
        }
        int highest_seen_value = Integer.MIN_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final Board imaginary = board.createCopy();
            final MoveStatus status = Player.makeMove(imaginary, move);
            if (status == MoveStatus.DONE) {
                final int current_value = min(imaginary, depth - 1);
                if (current_value >= highest_seen_value) {
                    highest_seen_value = current_value;
                }
            }
        }
        return highest_seen_value;
    }

}
