package engine.pieces;

import java.util.ArrayList;
import java.util.List;

import engine.Alliance;
import engine.board.Board;
import engine.board.Move;
import engine.board.Tile;
import engine.board.Move.AttackMove;

public final class Rook extends Piece {

    private final static int[] candidateMoveCoordinates = { -8, -1, 1, 8 };

    public Rook(final Alliance alliance) {
        super(Type.ROOK, alliance);
    }

    private Rook(final Rook rook) {
        super(rook);
    }

    @Override
    public List<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        int candidateDestinationCoordinate;
        for (final int currentCandidate : candidateMoveCoordinates) {
            candidateDestinationCoordinate = this.piecePosition;
            while (true) {
                if (isColumnExclusion(currentCandidate, candidateDestinationCoordinate)) {
                    break;
                }
                candidateDestinationCoordinate += currentCandidate;
                if (!Board.isValidTileCoordinate(candidateDestinationCoordinate)) {
                    break;
                } else {
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
                    if (!candidateDestinationTile.isTileOccupied()) {
                        legalMoves.add(new Move(this.piecePosition, candidateDestinationCoordinate, this));
                    } else {
                        final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                        final Alliance pieceAtDestinationAllegiance = pieceAtDestination.getPieceAlliance();
                        if (this.pieceAlliance != pieceAtDestinationAllegiance) {
                            legalMoves.add(new AttackMove(this.piecePosition, candidateDestinationCoordinate, this,
                                    pieceAtDestination));
                        }
                        break;
                    }
                }
            }
        }
        return legalMoves;
    }

    @Override
    public int getPieceValue() {
        return Type.ROOK.getPieceValue();
    }

    @Override
    public int locationBonus() {
        return this.pieceAlliance.rookBonus(this.piecePosition);
    }

    @Override
    public Rook createCopy() {
        return new Rook(this);
    }

    @Override
    public String toString() {
        return Type.ROOK.toString();
    }

    private static boolean isColumnExclusion(final int currentCandidate, final int candidateDestinationCoordinate) {
        return (Board.FIRST_COLUMN[candidateDestinationCoordinate] && (currentCandidate == -1)) ||
               (Board.EIGHTH_COLUMN[candidateDestinationCoordinate] && (currentCandidate == 1));
    }

}