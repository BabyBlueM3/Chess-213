package chess;

import chess.Piece;
import chess.PieceType;

public class Board {

    // Define the chessboard as a 2D array of Piece objects
    private Piece[][] board = new Piece[8][8]; // Instance variable for the board

    // Method to initialize the chessboard
    public void initializeBoard() {
        // Reset the board to null
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }

        // Initialize the pieces for both players
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, false); // Black pawns
            board[6][i] = new Piece(PieceType.PAWN, true);  // White pawns
        }

        // Black pieces on rank 0
        board[0][0] = board[0][7] = new Piece(PieceType.ROOK, false);
        board[0][1] = board[0][6] = new Piece(PieceType.KNIGHT, false);
        board[0][2] = board[0][5] = new Piece(PieceType.BISHOP, false);
        board[0][3] = new Piece(PieceType.QUEEN, false);
        board[0][4] = new Piece(PieceType.KING, false);

        // White pieces on rank 7
        board[7][0] = board[7][7] = new Piece(PieceType.ROOK, true);
        board[7][1] = board[7][6] = new Piece(PieceType.KNIGHT, true);
        board[7][2] = board[7][5] = new Piece(PieceType.BISHOP, true);
        board[7][3] = new Piece(PieceType.QUEEN, true);
        board[7][4] = new Piece(PieceType.KING, true);
    }

    // Method to print the chessboard
    public void printBoard() {
        for (int i = 7; i >= 0; i--) {  // Print from rank 8 to 1
            for (int j = 0; j < 8; j++) {  // Iterate over the files a-h
                if (board[i][j] != null) {
                    System.out.print(board[i][j].toString() + " ");
                } else {
                    System.out.print("## ");
                }
            }
            System.out.println(i + 1);  // Print the rank number at the end of the row
        }

        // Print the file letters (a to h)
        System.out.println(" a  b  c  d  e  f  g  h");
    }

}
