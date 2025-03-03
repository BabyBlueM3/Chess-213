package chess;

public enum PieceType {
    PAWN("p"), ROOK("R"), KNIGHT("N"), BISHOP("B"), QUEEN("Q"), KING("K");

    public final String shortName;

    PieceType(String name) {
        this.shortName = name;
    }
}
