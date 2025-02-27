package chess;

public class Board {

    // Define the chessboard as a 2D array of Piece objects
    private Piece[][] board = new Piece[8][8]; // Instance variable for the board

    // Method to initialize the chessboard
    public void initializeBoard() {
        // Initialize the pieces for both players
        for (int i = 0; i < 8; i++) {
            // Set up pawns
            board[1][i] = new Piece(PieceType.PAWN, true);  // White pawns
            board[6][i] = new Piece(PieceType.PAWN, false); // Black pawns
        }

        // Set up the back ranks (pieces)
        PieceType[] backRowPieces = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        // Black pieces on rank 0 (row 7 in the array)
        for (int i = 0; i < 8; i++) {
            board[0][i] = new Piece(backRowPieces[i], true);  // White pieces on rank 8
            board[7][i] = new Piece(backRowPieces[i], false); // Black pieces on rank 1
        }
    }

    // Method to print the chessboard
    public void printBoard() {
        for (int i = 7; i >= 0; i--) {  // Print from rank 8 to 1
            for (int j = 0; j < 8; j++) {  // Iterate over the files a-h
                // Print the piece if not null, else print empty space or ##
                if (board[i][j] != null) {
                    System.out.print(board[i][j].toString() + " ");
                } else {
                    System.out.print(((i + j) % 2 == 0) ? "   " : "## "); // Alternating empty spaces
                }
            }
            System.out.println(i + 1);  // Print the rank number at the end of the row
        }

        // Print the file letters (a to h)
        System.out.println(" a  b  c  d  e  f  g  h");
    }
}
