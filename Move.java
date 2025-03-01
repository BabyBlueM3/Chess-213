package chess;

public class Move {
    public int fromRow, fromCol, toRow, toCol;
    public String promotion;

    public Move(int fromRow, int fromCol, int toRow, int toCol, String promotion) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.promotion = promotion;
    }
}
