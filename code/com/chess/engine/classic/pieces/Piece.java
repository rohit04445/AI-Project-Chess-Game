package com.chess.engine.classic.pieces;

import java.util.List;

import com.chess.engine.classic.Alliance;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.Move;

public abstract class Piece {

    private final Type pieceType;
    protected final Alliance pieceAlliance;
    protected int piecePosition;
    protected boolean isFirstMove;

    Piece(final Type type, final Alliance alliance) {
        this.pieceType = type;
        this.pieceAlliance = alliance;
        this.isFirstMove = true;
    }

    Piece(final Piece p) {
        this.pieceType = p.getPieceType();
        this.pieceAlliance = p.getPieceAlliance();
        this.piecePosition = p.getPiecePosition();
        this.isFirstMove = p.isFirstMove();
    }

    public Type getPieceType() {
        return this.pieceType;
    }

    public Alliance getPieceAlliance() {
        return this.pieceAlliance;
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    public abstract int getPieceValue();

    public abstract int locationBonus();

    public abstract Piece createCopy();

    public boolean isKing() {
        return this.pieceType == Type.KING;
    }

    public void setPiecePosition(final int piece_position) {
        this.piecePosition = piece_position;
    }

    public void setIsFirstMove(final boolean firstMove) {
        this.isFirstMove = firstMove;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + this.pieceType.hashCode() + this.pieceAlliance.hashCode()
                + this.piecePosition;
        return hash;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }

        final Piece otherPiece = (Piece) other;

        return (this.pieceType == otherPiece.getPieceType())
                && (this.pieceAlliance == otherPiece.getPieceAlliance())
                && (this.piecePosition == otherPiece.getPiecePosition());

    }

    public abstract List<Move> calculateLegalMoves(final Board b);

    public enum Type {

        PAWN(100, "P"),
        KNIGHT(300, "N"),
        BISHOP(300, "B"),
        ROOK(500, "R"),
        QUEEN(900, "Q"),
        KING(10000, "K");

        private final int value;
        private final String pieceName;

        public int getPieceValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        Type(final int val, final String pieceName) {
            this.value = val;
            this.pieceName = pieceName;
        }

    }

}