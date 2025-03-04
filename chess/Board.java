package chess;

import chess.ReturnPlay;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
    }

    public void initialize() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = null;
            }
        }
        // Initialize pieces (simplified)
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, false);
            board[6][i] = new Piece(PieceType.PAWN, true);
        }
        board[0][0] = board[0][7] = new Piece(PieceType.ROOK, false);
        board[7][0] = board[7][7] = new Piece(PieceType.ROOK, true);
        board[0][1] = board[0][6] = new Piece(PieceType.KNIGHT, false);
        board[7][1] = board[7][6] = new Piece(PieceType.KNIGHT, true);
        board[0][2] = board[0][5] = new Piece(PieceType.BISHOP, false);
        board[7][2] = board[7][5] = new Piece(PieceType.BISHOP, true);
        board[0][3] = new Piece(PieceType.QUEEN, false);
        board[7][3] = new Piece(PieceType.QUEEN, true);
        board[0][4] = new Piece(PieceType.KING, false);
        board[7][4] = new Piece(PieceType.KING, true);
    }

    public boolean attemptMove(String from, String to, String promote, Chess.Player player) {
        int[] fromRC = notationToRC(from);
        int[] toRC = notationToRC(to);
        if (fromRC == null || toRC == null) return false;

        Piece movingPiece = board[fromRC[0]][fromRC[1]];
        if (movingPiece == null || movingPiece.isWhite != (player == Chess.Player.white)) {
            return false;
        }

        board[toRC[0]][toRC[1]] = movingPiece;
        board[fromRC[0]][fromRC[1]] = null;
        return true;
    }

    public boolean isInCheck(Chess.Player player) {
        return false; // Placeholder logic
    }

    public boolean canMove(Chess.Player player) {
        return true; // Placeholder logic
    }


    private int[] notationToRC(String square) {
        if (square.length() != 2) return null;
        int col = square.charAt(0) - 'a';
        int row = 8 - (square.charAt(1) - '0');
        return (col >= 0 && col < 8 && row >= 0 && row < 8) ? new int[]{row, col} : null;
    }

    private String rcToNotation(int row, int col) {
        return "" + (char) ('a' + col) + (8 - row);
    }
}
