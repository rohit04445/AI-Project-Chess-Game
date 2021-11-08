package engine.classic.pieces;

import java.util.ArrayList;
import java.util.List;

import engine.classic.Alliance;
import engine.classic.board.Board;
import engine.classic.board.Move;
import engine.classic.board.Move.AttackMove;
import engine.classic.board.Move.PawnJump;
import engine.classic.board.Move.PawnPromotion;
import engine.classic.board.Tile;

public final class Pawn
        extends Piece {

    private final static int[] candidateMoveCoordinates = {8, 16, 7, 9};

    public Pawn(final Alliance allegiance) {
        super(Type.PAWN, allegiance);
    }

    private Pawn(final Pawn pawn) {
        super(pawn);
    }

    @Override
    public int locationBonus() {
        return this.pieceAlliance.pawnBonus(this.piecePosition);
    }

    @Override
    public List<Move> calculateLegalMoves(final Board board) {

        final List<Move> legalMoves = new ArrayList<>();
        int candidateDestinationCoordinate;

        for (final int currentCandidate : candidateMoveCoordinates) {
            candidateDestinationCoordinate =
                    this.piecePosition + (this.pieceAlliance.getDirection() * currentCandidate * -1);
            if (!Board.isValidTileCoordinate(candidateDestinationCoordinate)) {
                continue;
            }
            final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
            if (currentCandidate == 8) {
                if (!candidateDestinationTile.isTileOccupied()) {
                    if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                        legalMoves.add(new PawnPromotion(new Move(this.piecePosition, candidateDestinationCoordinate, this)));
                    } else {
                        legalMoves.add(new Move(this.piecePosition, candidateDestinationCoordinate, this));
                    }
                }
            }
            else if (currentCandidate == 16 && this.isFirstMove() &&
                    ((Board.SECOND_ROW[this.piecePosition] && this.pieceAlliance.isBlack()) ||
                            (Board.SEVENTH_ROW[this.piecePosition] && this.pieceAlliance.isWhite()))) {
                final int behindCandidateDestinationCoordinate =
                        this.piecePosition + (this.pieceAlliance.getDirection() * 8 * -1);
                final Tile behindCandidateDestinationTile = board.getTile(behindCandidateDestinationCoordinate);
                if (!candidateDestinationTile.isTileOccupied() &&
                        !behindCandidateDestinationTile.isTileOccupied()) {
                    legalMoves.add(new PawnJump(this.piecePosition, candidateDestinationCoordinate, this));
                }
            }
            else if (currentCandidate == 7) {
                if ((Board.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite()) ||
                        (Board.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack())) {
                    continue;
                }
                if (candidateDestinationTile.isTileOccupied()) {
                    final Piece pieceOnCandidate = candidateDestinationTile.getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new PawnPromotion(new AttackMove(this.piecePosition, candidateDestinationCoordinate, this,
                                    pieceOnCandidate)));
                        } else {
                            legalMoves.add(new AttackMove(this.piecePosition, candidateDestinationCoordinate, this,
                                    pieceOnCandidate));
                        }
                    }
                }
            }
            else if (currentCandidate == 9) {
                if ((Board.FIRST_COLUMN[this.piecePosition] && this.pieceAlliance.isWhite()) ||
                        (Board.EIGHTH_COLUMN[this.piecePosition] && this.pieceAlliance.isBlack())) {
                    continue;
                }
                if (candidateDestinationTile.isTileOccupied()) {
                    final Piece pieceOnCandidate = candidateDestinationTile.getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        if(this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new PawnPromotion(new AttackMove(this.piecePosition, candidateDestinationCoordinate, this,
                                    pieceOnCandidate)));
                        } else {
                            legalMoves.add(new AttackMove(this.piecePosition, candidateDestinationCoordinate, this,
                                    pieceOnCandidate));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    @Override
    public int getPieceValue() {
        return Type.PAWN.getPieceValue();
    }

    @Override
    public String toString() {
        return Type.PAWN.toString();
    }

    @Override
    public Pawn createCopy() {
        return new Pawn(this);
    }

}