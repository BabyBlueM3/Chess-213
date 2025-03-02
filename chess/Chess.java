package chess;

import java.util.ArrayList;
import java.util.Scanner;

public class Chess {

    enum Player { white, black }
    private static Player currentPlayer;
    private static ArrayList<ReturnPiece> board;

    /**
     * Plays the next move for whichever player has the turn.
     *
     * @param move String for the next move, e.g., "a2 a3"
     * @return A ReturnPlay instance that contains the result of the move.
     */
    public static ReturnPlay play(String move) {
        move = move.trim();
        String[] parts = move.split(" ");
        ReturnPlay result = new ReturnPlay();
        result.piecesOnBoard = new ArrayList<>(board);
        result.message = null;
        
        if (move.equals("resign")) {
            result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.RESIGN_BLACK_WINS : ReturnPlay.Message.RESIGN_WHITE_WINS;
            return result;
        }
        
        if (parts.length == 2 || (parts.length == 3 && parts[2].equals("draw?"))) {
            ReturnPiece piece = getPieceAt(parts[0]);
            if (piece == null || !isCurrentPlayerPiece(piece) || !isValidMove(piece, parts[1])) {
                result.message = ReturnPlay.Message.ILLEGAL_MOVE;
                return result;
            }
            
            movePiece(piece, parts[1]);
            
            if (isKingCaptured()) {
                result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.CHECKMATE_WHITE_WINS : ReturnPlay.Message.CHECKMATE_BLACK_WINS;
                return result;
            }
            
            result.piecesOnBoard = new ArrayList<>(board);
            
            if (isCheck()) {
                result.message = ReturnPlay.Message.CHECK;
            }
            
            if (parts.length == 3 && parts[2].equals("draw?")) {
                result.message = ReturnPlay.Message.DRAW;
            }
            
            switchPlayer();
            return result;
        }
        
        result.message = ReturnPlay.Message.ILLEGAL_MOVE;
        return result;
    }
    
    private static void initializeBoard() {
        String[] pieceOrder = {"R", "N", "B", "Q", "K", "B", "N", "R"};
        for (int i = 0; i < 8; i++) {
            board.add(new ReturnPiece(ReturnPiece.PieceType.valueOf("WP"), ReturnPiece.PieceFile.values()[i], 2));
            board.add(new ReturnPiece(ReturnPiece.PieceType.valueOf("BP"), ReturnPiece.PieceFile.values()[i], 7));
            board.add(new ReturnPiece(ReturnPiece.PieceType.valueOf("W" + pieceOrder[i]), ReturnPiece.PieceFile.values()[i], 1));
            board.add(new ReturnPiece(ReturnPiece.PieceType.valueOf("B" + pieceOrder[i]), ReturnPiece.PieceFile.values()[i], 8));
        }
    }
    
    private static ReturnPiece getPieceAt(String position) {
        char file = position.charAt(0);
        int rank = Character.getNumericValue(position.charAt(1));
        for (ReturnPiece piece : board) {
            if (piece.pieceFile.toString().charAt(0) == file && piece.pieceRank == rank) {
                return piece;
            }
        }
        return null;
    }
    
    private static boolean isCurrentPlayerPiece(ReturnPiece piece) {
        return (currentPlayer == Player.white && piece.pieceType.toString().startsWith("W")) ||
               (currentPlayer == Player.black && piece.pieceType.toString().startsWith("B"));
    }

	private static boolean isPathClear(ReturnPiece piece, String destination, boolean isVertical) {
		int startFile = piece.pieceFile.ordinal();
		int startRank = piece.pieceRank;
		int destFile = destination.charAt(0) - 'a';
		int destRank = Character.getNumericValue(destination.charAt(1));
	
		if (isVertical) { // Moving vertically along the same file
			int minRank = Math.min(startRank, destRank);
			int maxRank = Math.max(startRank, destRank);
			for (int rank = minRank + 1; rank < maxRank; rank++) {
				if (getPieceAt("" + (char) ('a' + startFile) + rank) != null) {
					return false; // There's a piece in the way
				}
			}
		} else { // Moving horizontally along the same rank
			int minFile = Math.min(startFile, destFile);
			int maxFile = Math.max(startFile, destFile);
			for (int file = minFile + 1; file < maxFile; file++) {
				if (getPieceAt("" + (char) ('a' + file) + startRank) != null) {
					return false; // There's a piece in the way
				}
			}
		}
	
		return true; // No pieces blocking the path
	}
	
    
    private static boolean isValidMove(ReturnPiece piece, String destination) {
		int destFile = destination.charAt(0) - 'a';
		int destRank = Character.getNumericValue(destination.charAt(1));
		int fileDiff = Math.abs(destFile - piece.pieceFile.ordinal());
		int rankDiff = Math.abs(destRank - piece.pieceRank);
	
		switch (piece.pieceType) {
			case WP: // White Pawn
				return (fileDiff == 0 && destRank == piece.pieceRank + 1) ||
					   (piece.pieceRank == 2 && fileDiff == 0 && destRank == 4);
			case BP: // Black Pawn
				return (fileDiff == 0 && destRank == piece.pieceRank - 1) ||
					   (piece.pieceRank == 7 && fileDiff == 0 && destRank == 5);
			case WR: case BR: // Rook
				// Check if the rook is moving in a straight line (either same file or same rank)
				if (fileDiff == 0) { // Same file
					// Check if there are no pieces in between (same file, moving vertically)
					return isPathClear(piece, destination, true);
				} else if (rankDiff == 0) { // Same rank
					// Check if there are no pieces in between (same rank, moving horizontally)
					return isPathClear(piece, destination, false);
				}
				return false;  // Rook can only move in a straight line
			case WN: case BN: // Knight
				return (fileDiff == 2 && rankDiff == 1) || (fileDiff == 1 && rankDiff == 2);
			case WB: case BB: // Bishop
				return (fileDiff == rankDiff);
			case WQ: case BQ: // Queen
				return (fileDiff == rankDiff || fileDiff == 0 || rankDiff == 0);
			case WK: case BK: // King
				return fileDiff <= 1 && rankDiff <= 1;
			default:
				return false;
		}
	}
	
    
    private static boolean isKingCaptured() {
        boolean whiteKingExists = false;
        boolean blackKingExists = false;
        
        for (ReturnPiece piece : board) {
            if (piece.pieceType == ReturnPiece.PieceType.WK) {
                whiteKingExists = true;
            }
            if (piece.pieceType == ReturnPiece.PieceType.BK) {
                blackKingExists = true;
            }
        }
        return !whiteKingExists || !blackKingExists;
    }
    
    private static void movePiece(ReturnPiece piece, String destination) {
        char newFile = destination.charAt(0);
        int newRank = Character.getNumericValue(destination.charAt(1));
        
        board.remove(piece);
        board.add(new ReturnPiece(piece.pieceType, ReturnPiece.PieceFile.values()[newFile - 'a'], newRank));
    }
    
    private static boolean isCheck() {
        return false;
    }
    
    private static void switchPlayer() {
        currentPlayer = (currentPlayer == Player.white) ? Player.black : Player.white;
    }

	// Print the current state of the board
    public static void printBoard() {
        String[][] boardArray = makeBlankBoard();
        printPiecesOnBoard(board, boardArray);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                System.out.print(boardArray[r][c] + " ");
            }
            System.out.println(8 - r);  // Rank number (1 to 8)
        }

        System.out.println(" a  b  c  d  e  f  g  h");  // File labels (a to h)
    }

    // Generate a blank board layout
    static String[][] makeBlankBoard() {
        String[][] board = new String[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (r % 2 == 0) {
                    board[r][c] = c % 2 == 0 ? "  " : "##";
                } else {
                    board[r][c] = c % 2 == 0 ? "##" : "  ";
                }
            }
        }
        return board;
    }

    // Place the pieces on the board
    static void printPiecesOnBoard(ArrayList<ReturnPiece> pieces, String[][] board) {
        for (ReturnPiece rp : pieces) {
            int file = ("" + rp.pieceFile).charAt(0) - 'a';
            String pieceStr = "" + rp.pieceType;
            String ppstr = "" + Character.toLowerCase(pieceStr.charAt(0));
            ppstr += pieceStr.charAt(1) == 'P' ? 'p' : pieceStr.charAt(1);
            board[8 - rp.pieceRank][file] = ppstr;
        }
    }

    /**
     * This method should reset the game, and start from scratch.
     */
    public static void start() {
        board = new ArrayList<>();
        currentPlayer = Player.white;
        initializeBoard();
		
    }

    public static void main(String[] args) {
    start();
    Scanner scanner = new Scanner(System.in);
    
    while (true) {
        System.out.println("Current board:");
		printBoard();
        
        System.out.println("Player " + (currentPlayer == Player.white ? "White" : "Black") + "'s turn.");
        System.out.print("Enter your move (or 'quit' to exit): ");
        
        String move = scanner.nextLine().trim();
        
        // If the user wants to quit the game
        if (move.equals("quit")) {
            System.out.println("Game over. Thanks for playing!");
            break;
        }
        
        // If the user wants to resign
        if (move.equals("resign")) {
            ReturnPlay result = play(move);
            System.out.println(result.message);  // Print resignation message
            break;
        }

		if (move.equals("reset")) {
            start();  // Reset the board and game state
            System.out.println("Game has been reset.");
            continue; // Skip the rest of the loop
        }
		

        // Process the move and display the result
        ReturnPlay result = play(move);
        
        if (result.message != null) {
            System.out.println(result.message); // Print result message (check, checkmate, etc.)
        }
    }
    
    scanner.close(); // Close the scanner when the game ends
}
}
