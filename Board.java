package chess;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public class ChessBoard {
        // Define the chessboard as a 2D array
        private static final String[][] board = new String[8][8];
    
        // Method to initialize the chessboard
        public static void initializeBoard() {
            // White pieces on ranks 7 and 8
            board[7][0] = "R"; // White Rook
            board[7][1] = "N"; // White Knight
            board[7][2] = "B"; // White Bishop
            board[7][3] = "Q"; // White Queen
            board[7][4] = "K"; // White King
            board[7][5] = "B"; // White Bishop
            board[7][6] = "N"; // White Knight
            board[7][7] = "R"; // White Rook
            for (int i = 0; i < 8; i++) {
                board[6][i] = "P"; // White Pawn on rank 7
            }
    
            // Black pieces on ranks 1 and 2
            board[0][0] = "r"; // Black Rook
            board[0][1] = "n"; // Black Knight
            board[0][2] = "b"; // Black Bishop
            board[0][3] = "q"; // Black Queen
            board[0][4] = "k"; // Black King
            board[0][5] = "b"; // Black Bishop
            board[0][6] = "n"; // Black Knight
            board[0][7] = "r"; // Black Rook
            for (int i = 0; i < 8; i++) {
                board[1][i] = "p"; // Black Pawn on rank 2
            }
    
            // Empty squares ('.') for ranks 3 to 6
            for (int i = 2; i < 6; i++) {
                for (int j = 0; j < 8; j++) {
                    board[i][j] = "."; // Empty square
                }
            }
        }
    
        // Method to print the chessboard
        public static void printBoard() {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    System.out.print(board[i][j] + " ");
                }
                System.out.println();
            }
        }
    }
    
    
}
