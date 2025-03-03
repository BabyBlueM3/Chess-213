package chess;

public class Piece {
    public PieceType type;
    public boolean isWhite;

    public Piece(PieceType type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
    }

    public String toString() {
        return (isWhite ? "w" : "b") + type.shortName;
    }
}
