package chess;

import java.io.File;
import java.io.FileNotFoundException;
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
	
			if (piece == null || !isCurrentPlayerPiece(piece) || !isValidMove(piece, parts[1]) || isCheck(false)) {
				result.message = ReturnPlay.Message.ILLEGAL_MOVE;
				return result;
			}
	
			// Move the piece
			movePiece(piece, parts[1]);

			// Switch turn after move
			switchPlayer();

			// Check if the opponent is in check
			if (isCheck(false)) {
				// Check if the threatened King has any valid escape moves
				if (isCheck(true)) {
					result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.CHECKMATE_WHITE_WINS : ReturnPlay.Message.CHECKMATE_BLACK_WINS;
				} else {
					result.message = ReturnPlay.Message.CHECK;
				}
				return result;
			}

			// Handle draw request
			if (parts.length == 3 && parts[2].equals("draw?")) {
				result.message = ReturnPlay.Message.DRAW;
			}


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

	private static boolean isPathDiagonalClear(ReturnPiece piece, String destination) {
		int startFile = piece.pieceFile.ordinal();
		int startRank = piece.pieceRank;
		int destFile = destination.charAt(0) - 'a';
		int destRank = Character.getNumericValue(destination.charAt(1));
	
		if (Math.abs(destFile - startFile) != Math.abs(destRank - startRank)) {
			return false; // Not moving diagonally
		}
	
		int fileStep = (destFile > startFile) ? 1 : -1;
		int rankStep = (destRank > startRank) ? 1 : -1;
	
		int file = startFile + fileStep;
		int rank = startRank + rankStep;
		while (file != destFile && rank != destRank) {
			if (getPieceAt("" + (char) ('a' + file) + rank) != null) {
				return false; // There's a piece in the way
			}
			file += fileStep;
			rank += rankStep;
		}
	
		return true;
	}	
	
    
    private static boolean isValidMove(ReturnPiece piece, String destination) {
		int destFile = destination.charAt(0) - 'a';
		int destRank = Character.getNumericValue(destination.charAt(1));
		int fileDiff = Math.abs(destFile - piece.pieceFile.ordinal());
		int rankDiff = Math.abs(destRank - piece.pieceRank);
	
		ReturnPiece targetPiece = getPieceAt(destination);
		boolean isCapturing = targetPiece != null && !isCurrentPlayerPiece(targetPiece);
	
		switch (piece.pieceType) {
			case WP: // White Pawn
				if (fileDiff == 0 && destRank == piece.pieceRank + 1 && targetPiece == null) {
					return true; // Normal move forward
				}
				if (fileDiff == 0 && piece.pieceRank == 2 && destRank == 4 && targetPiece == null) {
					return true; // Double move from starting position
				}
				if (fileDiff == 1 && destRank == piece.pieceRank + 1 && isCapturing) {
					return true; // Capturing diagonally
				}
				return false;
			case BP: // Black Pawn
				if (fileDiff == 0 && destRank == piece.pieceRank - 1 && targetPiece == null) {
					return true; // Normal move forward
				}
				if (fileDiff == 0 && piece.pieceRank == 7 && destRank == 5 && targetPiece == null) {
					return true; // Double move from starting position
				}
				if (fileDiff == 1 && destRank == piece.pieceRank - 1 && isCapturing) {
					return true; // Capturing diagonally
				}
				return false;
			case WR: case BR: // Rook
				if (fileDiff == 0) { // Moving vertically
					return isPathClear(piece, destination, true) && (targetPiece == null || isCapturing);
				}
				if (rankDiff == 0) { // Moving horizontally
					return isPathClear(piece, destination, false) && (targetPiece == null || isCapturing);
				}
				return false;
			case WN: case BN: // Knight
				return (fileDiff == 2 && rankDiff == 1) || (fileDiff == 1 && rankDiff == 2);
			case WB: case BB: // Bishop
				return (fileDiff == rankDiff) && isPathDiagonalClear(piece, destination) && (targetPiece == null || isCapturing);
			case WQ: case BQ: // Queen
				if (fileDiff == rankDiff) { // Moving diagonally
					return isPathDiagonalClear(piece, destination) && (targetPiece == null || isCapturing);
				}
				if (fileDiff == 0 || rankDiff == 0) { // Moving straight
					return isPathClear(piece, destination, fileDiff == 0) && (targetPiece == null || isCapturing);
				}
				return false;
			case WK: case BK: // King
				return fileDiff <= 1 && rankDiff <= 1 && (targetPiece == null || isCapturing);
			default:
				return false;
		}
	}
	

    private static void movePiece(ReturnPiece piece, String destination) {
        char newFile = destination.charAt(0);
        int newRank = Character.getNumericValue(destination.charAt(1));
        
        board.remove(piece);
        board.add(new ReturnPiece(piece.pieceType, ReturnPiece.PieceFile.values()[newFile - 'a'], newRank));
    }

    private static boolean isCheck(boolean isMate) {
		// Find the king's position
		ReturnPiece.PieceType kingType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WK : ReturnPiece.PieceType.BK;
		ReturnPiece king = null;
		
		for (ReturnPiece piece : board) {
			if (piece.pieceType == kingType) {
				king = piece;
				break;
			}
		}
		
		// If no king is found (which should never happen), return false
		if (king == null) {
			return false;
		}


		if(isMate) { // Check if king has any legal moves by moving it to every possible position and seeing if its in check.
			String destination = "";
			for (int f = -1; f <= 1; f++) { // f for File
				for (int r = -1; r <= 1; r++) { // r for Rank
					switch (r) {
						case -1: // Check the left edge
							if (king.toString().charAt(0) != 'a') { // Check if king is on the left edge
								destination = "" + (king.toString().charAt(0) - 'a') + (Character.getNumericValue(king.toString().charAt(1)) + r);
							}
							break;
						case 1: // Check the right edge
							if (king.toString().charAt(0) != 'h') { // Check if king is on the right edge
								destination = "" + (king.toString().charAt(0) + 'a') + (Character.getNumericValue(king.toString().charAt(1)) + r);
							}
							break;
						default:
							destination = "" + king.toString().charAt(0) + (Character.getNumericValue(king.toString().charAt(1)) + r);
							break;
					}
					if(isValidMove(king, destination)) { // If the king is able to survive at least one potential move, it is not in checkmate
						boolean isSafe = true;
						for (ReturnPiece piece : board) {
							if (piece.pieceType.toString().charAt(0) != kingType.toString().charAt(0) && isValidMove(piece, destination)) {
								isSafe = false;
								break;  // The king is in check so check it's next position
							}
						}
						if(isSafe) {
							return false; // The king is NOT in checkmate
						}
					}
					destination = null;
				}
			}
			return true; // No legal moves, so the king is in checkmate


		} else { // Check if any opponent piece can attack the king's position
			for (ReturnPiece piece : board) {
				if (piece.pieceType.toString().charAt(0) != kingType.toString().charAt(0) && isValidMove(piece, king.pieceFile.toString() + king.pieceRank)) {
					return true;  // The king is in check
				}
			}
		}
		return false; // No threats to the king
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
		// Check if a file path is provided as an argument
		if (args.length < 1) {
			System.out.println("Please provide a file path with chess moves.");
			return;
		}
	
		// Read moves from the file
		String filePath = args[0];
		File moveFile = new File(filePath);
	
		if (!moveFile.exists()) {
			System.out.println("The specified file does not exist.");
			return;
		}
	
		try (Scanner fileScanner = new Scanner(moveFile)) {
			start(); // Initialize the board and set up the game
	
			while (fileScanner.hasNextLine()) {
				String move = fileScanner.nextLine().trim();
				
				// Print current board before processing the move
				System.out.println("Current board state:");
				printBoard();
				System.out.println(currentPlayer + "'s turn\n");
	
				// Process the move
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
	
				// Print the current board after each move
				System.out.println("This is the current board output:");
				printBoard();
				System.out.println(currentPlayer + "'s turn\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error reading the file: " + e.getMessage());
		}
	}
	

}