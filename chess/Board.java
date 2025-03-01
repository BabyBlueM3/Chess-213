package chess;

public class Board {

    private Piece[][] board = new Piece[8][8];

    public void initializeBoard() {
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, true);  // White pawns
            board[6][i] = new Piece(PieceType.PAWN, false); // Black pawns
        }

        PieceType[] backRowPieces = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int i = 0; i < 8; i++) {
            board[0][i] = new Piece(backRowPieces[i], true);  
            board[7][i] = new Piece(backRowPieces[i], false);
        }
    }

    public void printBoard() {
        for (int i = 7; i >= 0; i--) {  
            for (int j = 0; j < 8; j++) {  
                if (board[i][j] != null) {
                    System.out.print(board[i][j].toString() + " ");
                } else {
                    System.out.print(((i + j) % 2 == 0) ? "   " : "## "); 
                }
            }
            System.out.println(i + 1);
        }
        System.out.println(" a  b  c  d  e  f  g  h");
    }

    /** ✅ Get a piece at a given position */
    public Piece getPiece(int x, int y) {
        if (isOutOfBounds(x, y)) return null;
        return board[x][y];
    }

    /** ✅ Move a piece */
    public void movePiece(int startX, int startY, int endX, int endY) {
        board[endX][endY] = board[startX][startY]; 
        board[startX][startY] = null; 
    }

    /** ✅ Validate move */
    public boolean isValidMove(int startX, int startY, int endX, int endY, Player currentPlayer) {
        Piece piece = getPiece(startX, startY);
        if (piece == null || piece.getPlayer() != currentPlayer) {
            return false;
        }

        if (!piece.isValidMove(startX, startY, endX, endY, this)) {
            return false;
        }

        if (isPathObstructed(startX, startY, endX, endY, piece.getType())) {
            return false;
        }

        Piece destinationPiece = getPiece(endX, endY);
        if (destinationPiece != null && destinationPiece.getPlayer() == currentPlayer) {
            return false;
        }

        return true;
    }

    /** ✅ Check if path is clear */
    private boolean isPathObstructed(int startX, int startY, int endX, int endY, PieceType type) {
        if (type == PieceType.KNIGHT) return false;

        int dx = Integer.compare(endX, startX);
        int dy = Integer.compare(endY, startY);
        int x = startX + dx, y = startY + dy;

        while (x != endX || y != endY) {
            if (getPiece(x, y) != null) return true;
            x += dx;
            y += dy;
        }
        return false;
    }

    /** ✅ Check if a player is in check */
    public boolean isCheck(Player player) {
        int kingX = -1, kingY = -1;
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = getPiece(i, j);
                if (piece instanceof King && piece.getPlayer() == player) {
                    kingX = i;
                    kingY = j;
                    break;
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = getPiece(i, j);
                if (piece != null && piece.getPlayer() != player) {
                    if (piece.isValidMove(i, j, kingX, kingY, this)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** ✅ Check if a player is in checkmate */
    public boolean isCheckmate(Player player) {
        if (!isCheck(player)) return false;

        for (int startX = 0; startX < 8; startX++) {
            for (int startY = 0; startY < 8; startY++) {
                Piece piece = getPiece(startX, startY);
                if (piece != null && piece.getPlayer() == player) {
                    for (int endX = 0; endX < 8; endX++) {
                        for (int endY = 0; endY < 8; endY++) {
                            if (isValidMove(startX, startY, endX, endY, player)) {
                                Piece temp = getPiece(endX, endY);
                                movePiece(startX, startY, endX, endY);
                                boolean stillCheck = isCheck(player);
                                movePiece(endX, endY, startX, startY);
                                board[endX][endY] = temp;
                                if (!stillCheck) return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /** ✅ Helper method to check bounds */
    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= 8 || y < 0 || y >= 8;
    }
}
