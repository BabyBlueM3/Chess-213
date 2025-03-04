// Will Tran & Jack Loyd


package chess;

import chess.ReturnPiece;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chess {

    enum Player { white, black }
    private static Player currentPlayer;
    private static ArrayList<ReturnPiece> board;
	private static ReturnPiece enPassant = null; // If a pawn is ever moved from the start row, record it so the next move can check for an En Passant
	private static boolean canWhiteCastleQueen = true;
	private static boolean canWhiteCastleKing = true;
	private static boolean canBlackCastleKing = true;
	private static boolean canBlackCastleQueen = true;
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
	
	
		if (parts.length == 2 || (parts.length == 3 && (parts[2].equals("draw?")) || parts[2].length() == 1)) {
			ReturnPiece piece = getPieceAt(parts[0]);
	
			if (piece == null || !isCurrentPlayerPiece(piece) || !isValidMove(piece, parts[1], false)) {
				result.message = ReturnPlay.Message.ILLEGAL_MOVE;
				return result;
			}

			// check if move will put moved king in check
			if (piece.pieceType.toString().charAt(1) == 'K') {
				char newFile = parts[1].charAt(0);
				int newRank = Character.getNumericValue(parts[1].charAt(1));

				ReturnPiece.PieceType kingType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WK : ReturnPiece.PieceType.BK;

				ReturnPiece king = new ReturnPiece();
				king.pieceType = kingType;
				king.pieceFile = ReturnPiece.PieceFile.values()[newFile - 'a'];
				king.pieceRank = newRank;

				if (isCheck(king)) {
					result.message = ReturnPlay.Message.ILLEGAL_MOVE;
					return result;
				}
			}





			// Handle promotion
			if (parts.length == 3 && parts[2].length() == 1) {
				ReturnPiece tempPiece = new ReturnPiece();

				switch (parts[2]) {
					case "R":
						ReturnPiece.PieceType rookType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WR : ReturnPiece.PieceType.BR;
						// Add the piece to the new position
						tempPiece.pieceType = rookType;

						break;
					case "B":
						ReturnPiece.PieceType bishopType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WB : ReturnPiece.PieceType.BB;
						// Add the piece to the new position
						tempPiece.pieceType = bishopType;
						break;
					case "N":
						ReturnPiece.PieceType knightType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WN : ReturnPiece.PieceType.BN;
						tempPiece.pieceType = knightType;
						break;
					case "Q":
						ReturnPiece.PieceType queenType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WQ : ReturnPiece.PieceType.BQ;
						tempPiece.pieceType = queenType;
						break;
					default:
						ReturnPiece.PieceType defaultType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WQ : ReturnPiece.PieceType.BQ;
						tempPiece.pieceType = defaultType;
						break;
				}
				tempPiece.pieceFile = piece.pieceFile;
				tempPiece.pieceRank = piece.pieceRank;
				movePiece(tempPiece, parts[1]);
				board.remove(piece);
			} else {
				// Move the piece
				movePiece(piece, parts[1]);
			}


				// Switch turn after move
			switchPlayer();

			// Check if the opponent is in check
			ReturnPiece king = findKing();
			if (isCheck(king)) {
				// Check if the threatened King has any valid escape moves
				if (isCheckMate(king)) {
					result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.CHECKMATE_BLACK_WINS : ReturnPlay.Message.CHECKMATE_WHITE_WINS;
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
		String[] fileOrder = {"a", "b", "c", "d", "e", "f", "g", "h"};
		ReturnPiece tempPiece = new ReturnPiece();
        for (int i = 0; i < 8; i++) {

			ReturnPiece tempWP = new ReturnPiece();
			ReturnPiece tempBP = new ReturnPiece();
			ReturnPiece tempW = new ReturnPiece();
			ReturnPiece tempB = new ReturnPiece();

			tempWP.pieceType = ReturnPiece.PieceType.WP;
			tempBP.pieceType = ReturnPiece.PieceType.BP;
			switch (pieceOrder[i]) {
				case "R":
					tempW.pieceType = ReturnPiece.PieceType.WR;
					tempB.pieceType = ReturnPiece.PieceType.BR;
					break;
				case "N":
					tempW.pieceType = ReturnPiece.PieceType.WN;
					tempB.pieceType = ReturnPiece.PieceType.BN;
					break;
				case "B":
					tempW.pieceType = ReturnPiece.PieceType.WB;
					tempB.pieceType = ReturnPiece.PieceType.BB;
					break;
				case "Q":
					tempW.pieceType = ReturnPiece.PieceType.WQ;
					tempB.pieceType = ReturnPiece.PieceType.BQ;
					break;
				case "K":
					tempW.pieceType = ReturnPiece.PieceType.WK;
					tempB.pieceType = ReturnPiece.PieceType.BK;
					break;
			}

			switch (fileOrder[i]) {
				case "a":
					tempWP.pieceFile = ReturnPiece.PieceFile.a;
					tempBP.pieceFile = ReturnPiece.PieceFile.a;
					tempW.pieceFile = ReturnPiece.PieceFile.a;
					tempB.pieceFile = ReturnPiece.PieceFile.a;
					break;
				case "b":
					tempWP.pieceFile = ReturnPiece.PieceFile.b;
					tempBP.pieceFile = ReturnPiece.PieceFile.b;
					tempW.pieceFile = ReturnPiece.PieceFile.b;
					tempB.pieceFile = ReturnPiece.PieceFile.b;
					break;
				case "c":
					tempWP.pieceFile = ReturnPiece.PieceFile.c;
					tempBP.pieceFile = ReturnPiece.PieceFile.c;
					tempW.pieceFile = ReturnPiece.PieceFile.c;
					tempB.pieceFile = ReturnPiece.PieceFile.c;
					break;
				case "d":
					tempWP.pieceFile = ReturnPiece.PieceFile.d;
					tempBP.pieceFile = ReturnPiece.PieceFile.d;
					tempW.pieceFile = ReturnPiece.PieceFile.d;
					tempB.pieceFile = ReturnPiece.PieceFile.d;
					break;
				case "e":
					tempWP.pieceFile = ReturnPiece.PieceFile.e;
					tempBP.pieceFile = ReturnPiece.PieceFile.e;
					tempW.pieceFile = ReturnPiece.PieceFile.e;
					tempB.pieceFile = ReturnPiece.PieceFile.e;
					break;
				case "f":
					tempWP.pieceFile = ReturnPiece.PieceFile.f;
					tempBP.pieceFile = ReturnPiece.PieceFile.f;
					tempW.pieceFile = ReturnPiece.PieceFile.f;
					tempB.pieceFile = ReturnPiece.PieceFile.f;
					break;
				case "g":
					tempWP.pieceFile = ReturnPiece.PieceFile.g;
					tempBP.pieceFile = ReturnPiece.PieceFile.g;
					tempW.pieceFile = ReturnPiece.PieceFile.g;
					tempB.pieceFile = ReturnPiece.PieceFile.g;
					break;
				case "h":
					tempWP.pieceFile = ReturnPiece.PieceFile.h;
					tempBP.pieceFile = ReturnPiece.PieceFile.h;
					tempW.pieceFile = ReturnPiece.PieceFile.h;
					tempB.pieceFile = ReturnPiece.PieceFile.h;
					break;
			}

			tempWP.pieceRank = 2;
			tempBP.pieceRank = 7;
			tempW.pieceRank = 1;
			tempB.pieceRank = 8;

			board.add(tempWP);
			board.add(tempBP);
			board.add(tempW);
			board.add(tempB);
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
	

    private static boolean isValidMove(ReturnPiece piece, String destination, boolean isCapturingKing) { //since taking the king is technically an illegal move im gonna add this isCapturingKing to ignore that when checking for a check
		int destFile = destination.charAt(0) - 'a';
		int destRank = Character.getNumericValue(destination.charAt(1));
		int fileDiff = Math.abs(destFile - piece.pieceFile.ordinal());
		int rankDiff = Math.abs(destRank - piece.pieceRank);
	
		ReturnPiece targetPiece = getPieceAt(destination);
		boolean isCapturing = targetPiece != null && !isCurrentPlayerPiece(targetPiece) || isCapturingKing;


		switch (piece.pieceType) {
			case WP: // White Pawn
				if (fileDiff == 0 && destRank == piece.pieceRank + 1 && targetPiece == null) {
					return true; // Normal move forward
				}
				if (fileDiff == 0 && piece.pieceRank == 2 && destRank == 4 && targetPiece == null) {
//					System.out.println(currentPlayer + " double moved pawn: " + piece); //								debug
					return true; // Double move from starting position
				}
				if (fileDiff == 1 && destRank == piece.pieceRank + 1) {
//					System.out.println(currentPlayer + " diagonal capture with pawn: " + piece); //								debug
					if (enPassant != null) {
						enPassant = null;
//						System.out.println("\n\n en passant!!!!!!!!\n\n"); //							debug
						return true; // En Passant capture
					}
					return isCapturing; // Capturing diagonally
				}

				return false;
			case BP: // Black Pawn
				if (fileDiff == 0 && destRank == piece.pieceRank - 1 && targetPiece == null) {
					return true; // Normal move forward
				}
				if (fileDiff == 0 && piece.pieceRank == 7 && destRank == 5 && targetPiece == null) {
//					System.out.println(currentPlayer + " double moved pawn: " + piece); //								debug
					return true; // Double move from starting position
				}
				if (fileDiff == 1 && destRank == piece.pieceRank - 1) {
//					System.out.println(currentPlayer + " diagonal capture with pawn: " + piece); //								debug
					if (enPassant != null) {
						enPassant = null;
//						System.out.println("\n\n en passant!!!!!!!!\n\n"); //							debug
						return true; // En Passant capture
					}
					return isCapturing; // Capturing diagonally
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
			case WK:
				if(canWhiteCastleQueen && destination.equalsIgnoreCase("c1")) {
					return isPathClear(piece, "b1", false); // The white king is able to castle queenside
				}
				if(canWhiteCastleKing && destination.equalsIgnoreCase("g1")) {
					return isPathClear(piece, "g1", false); // The white king is able to castle queenside
				}
				return fileDiff <= 1 && rankDiff <= 1 && (targetPiece == null || isCapturing);
			case BK: // King
				if(canBlackCastleQueen && destination.equalsIgnoreCase("c8")) {
					return isPathClear(piece, "b8", false); // The black king is able to castle queenside
				}
				if(canBlackCastleKing && destination.equalsIgnoreCase("g8")) {
					return isPathClear(piece, "g8", false); // The black king is able to castle queenside
				}
				return fileDiff <= 1 && rankDiff <= 1 && (targetPiece == null || isCapturing);
			default:
				return false;
		}
	}


	private static void movePiece(ReturnPiece piece, String destination) {
		char destFile = destination.charAt(0);
		int destRank = Character.getNumericValue(destination.charAt(1));
		int fileDiff = Math.abs(destFile - piece.pieceFile.ordinal());
		int rankDiff = Math.abs(destRank - piece.pieceRank);

		// Remove the opponent's piece if it exists at the destination
		ReturnPiece targetPiece = getPieceAt(destination);
		if (targetPiece != null && !isCurrentPlayerPiece(targetPiece)) {
			board.remove(targetPiece);
		}

		//check for special cases
		switch (piece.pieceType) {
			case WP: // White Pawn
				if (fileDiff == 0 && piece.pieceRank == 2 && destRank == 4 && targetPiece == null) {
					enPassant = piece;
				}
				break;
			case BP: // Black Pawn
				if (fileDiff == 0 && piece.pieceRank == 7 && destRank == 5 && targetPiece == null) {
					enPassant = piece;
				}
				break;
			case WR: // White Rook
				//disable white castling on the side of the rook (either a or h)
				if (canWhiteCastleQueen && piece.toString().charAt(0) - 'a' == 0) {
					canWhiteCastleQueen = false;
				}
				if (canWhiteCastleKing && piece.toString().charAt(0) - 'a' == 7) {
					canWhiteCastleKing = false;
				}
				break;
			case BR: // Black Rook
				//disable black castling on the side of the rook
				if (canBlackCastleKing && piece.toString().charAt(0) - 'a' == 0) {
					canBlackCastleKing = false;
				}
				if (canBlackCastleQueen && piece.toString().charAt(0) - 'a' == 7) {
					canBlackCastleQueen = false;
				}
				break;
			case WK: case BK: // King
				// check for castle
				if (canWhiteCastleQueen && destination.charAt(0) == 'c'
						|| canWhiteCastleKing && destination.charAt(0) == 'g'
						|| canBlackCastleQueen && destination.charAt(0) == 'c'
						|| canBlackCastleKing && destination.charAt(0) == 'g') {

					// Move the rook for the castle (was gonna make it recursive but its a headache and a half because piece is null)
					ReturnPiece rook = getPieceAt("" + (destination.charAt(0) == 'c' ? 'a' : 'h') + destRank);
					board.remove(rook);
					ReturnPiece tempRook = new ReturnPiece();
					tempRook.pieceType = currentPlayer == Player.white ? ReturnPiece.PieceType.WR : ReturnPiece.PieceType.BR;
					tempRook.pieceFile = ReturnPiece.PieceFile.valueOf("" + (destination.charAt(0) == 'c' ? 'c' : 'f'));
					tempRook.pieceRank = destRank;
					board.add(tempRook);
				}

				if(currentPlayer == Player.white) { //disable white castling
					canWhiteCastleQueen = false;
					canWhiteCastleKing = false;
				} else {//disable black castling
					canBlackCastleKing = false;
					canBlackCastleQueen = false;
				}
				break;
		}


		// Remove the moving piece from its current position
		board.remove(piece);

		// Add the piece to the new position
		ReturnPiece temp = new ReturnPiece();
		temp.pieceType = piece.pieceType;
		temp.pieceFile = ReturnPiece.PieceFile.values()[destFile - 'a'];
		temp.pieceRank = destRank;
		board.add(temp);




/*		if (piece.pieceType == ReturnPiece.PieceType.WK || piece.pieceType == ReturnPiece.PieceType.BK) {
			// If it's a king, update the castling rights
			updateCastlingRights(piece.pieceType == ReturnPiece.PieceType.WK ? "K" : "k", startRow, startCol);
		}
		else if (piece.pieceType == ReturnPiece.PieceType.WR || piece.pieceType == ReturnPiece.PieceType.BR) {
			// If it's a rook, update the castling rights
			updateCastlingRights(piece.pieceType == ReturnPiece.PieceType.WR ? "R" : "r", startRow, startCol);
		}*/
	}

	private static ReturnPiece findKing() {
		// Find the king's position
		ReturnPiece.PieceType kingType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WK : ReturnPiece.PieceType.BK;

		for (ReturnPiece piece : board) {
			if (piece.pieceType == kingType) {
				return piece;
			}
		}
		// If no king found return null (should never happen)
		return null;
	}

	private static boolean isCheckMate(ReturnPiece king) {
//		System.out.println("checkmate? king: " + currentPlayer); // 									debug

		String destination = "";

		// Check if king has any legal moves by moving it to every possible position and seeing if its in check.
		for (int r = -1; r <= 1; r++) { // r for Rank
			for (int f = -1; f <= 1; f++) { // f for File
//				System.out.println("rank:" + (Character.getNumericValue(king.toString().charAt(1)) + r)); // 									debug
				if (Character.getNumericValue(king.toString().charAt(1)) + r >= 1 && Character.getNumericValue(king.toString().charAt(1)) + r <= 8) {
					switch (f) {
						case -1: // Check the left edge
							if (king.toString().charAt(0) != 'a') { // Check if king is on the left edge
//								System.out.println("file (letters): " + ReturnPiece.PieceFile.values()[(king.toString().charAt(0) - 'a') + f] + "\n"); // 									debug
								destination = "" + (ReturnPiece.PieceFile.values()[(king.toString().charAt(0) - 'a') + f]) + (Character.getNumericValue(king.toString().charAt(1)) + r);
							} else {
//								System.out.println(" left edge");// 									debug (remove the whole else statement)
							}
							break;
						case 1: // Check the right edge
							if (king.toString().charAt(0) != 'h') { // Check if king is on the right edge
//								System.out.println(" file (letters): " + (ReturnPiece.PieceFile.values()[(king.toString().charAt(0) - 'a') + f] + "\n")); // 									debug
								destination = "" + (ReturnPiece.PieceFile.values()[(king.toString().charAt(0) - 'a') + f]) + (Character.getNumericValue(king.toString().charAt(1)) + r);
							} else {
//								System.out.println(" right edge");// 									debug (remove the whole else statement)
							}
							break;
						default:
//							System.out.println(" file (letters): " + (ReturnPiece.PieceFile.values()[(king.toString().charAt(0) - 'a') + f] + "\n")); // 									debug
							destination = "" + ReturnPiece.PieceFile.values()[(king.toString().charAt(0) - 'a') + f] + (Character.getNumericValue(king.toString().charAt(1)) + r);
							break;
					}

//					System.out.println("destination: " + destination + "\nisValidMove: " + isValidMove(king, destination, false)); //							debug
					if (isValidMove(king, destination, false)) { // If the king is able to survive at least one potential move, it is not in checkmate
						boolean isSafe = true;
						for (ReturnPiece piece : board) {
//							System.out.print("\npiece: " + piece); //										debug
							if (piece.pieceType.toString().charAt(0) != king.pieceType.toString().charAt(0) && isValidMove(piece, destination, true)) {
								isSafe = false;
//								System.out.println(" check! move on. \n"); // 					debug
								break;  // The king is in check so check it's next position
							}
						}
						if (isSafe) {
//							System.out.println("\n\nNO CHECKMATE FOUND\n\n"); // 									debug
							return false; // The king is NOT in checkmate
						}
					}
				}
				destination = null;
			}
		}
//		System.out.println("CHECKMATE FOUND"); // 									debug
		return true; // No legal moves, so the king is in checkmate


	}



    private static boolean isCheck(ReturnPiece king) {
		 // Check if any opponent piece can attack the king's position
//		System.out.println("check? king: " + currentPlayer); //                         debug
		for (ReturnPiece piece : board) {
//			System.out.print(piece + " is valid move? " + (isValidMove(piece, king.pieceFile.toString() + king.pieceRank, true)) + "\n"); //					debug
			if (piece.pieceType.toString().charAt(0) != king.pieceType.toString().charAt(0) && isValidMove(piece, king.pieceFile.toString() + king.pieceRank, true)) {
//				System.out.println("CHECK FOUND"); //							debug
				return true;  // The king is in check
				}
			}
//		System.out.println("CHECK NOT FOUND"); // 									debug
		return false; // No threats to the king
	}

	/*public void updateCastlingRights(String piece, int startRow, int startCol) {
		switch (piece) {
			case "K": // White King moves
				whiteKingMoved = true;
				break;
			case "k": // Black King moves
				blackKingMoved = true;
				break;
			case "R": // White Rook moves
				if (startRow == 0 && startCol == 0) canWhiteCastleQueen; // Queenside (a1)
				if (startRow == 0 && startCol == 7)  = true; // Kingside (h1)
				break;
			case "r": // Black Rook moves
				if (startRow == 7 && startCol == 0) blackRookMoved[0] = true; // Queenside (a8)
				if (startRow == 7 && startCol == 7) blackRookMoved[1] = true; // Kingside (h8)
				break;
			default:
				break; // Do nothing for other pieces
		}
	}*/

	/*private static void executeCastling(ReturnPiece king, String destination) {
		// Determine rank based on piece color
		int rank = (king.pieceType == ReturnPiece.PieceType.WK) ? 1 : 8;

		// --- Validate Castling Path ---
		// Check the path between the king and the rook to ensure it's clear
		// For kingside castling, check if the squares between king and rook are clear (f1/h1)
		if (destination.equals("g" + rank)) { // Kingside castling
			// Check if path between king and rook is clear
			if (!isPathClear(king, "f" + rank, false)) { // Horizontal check between e1 and f1
				System.out.println("Path is not clear for kingside castling.");
				return;
			}
		} else if (destination.equals("c" + rank)) { // Queenside castling
			// Check if path between king and rook is clear
			if (!isPathClear(king, "d" + rank, false)) { // Horizontal check between e1 and d1
				System.out.println("Path is not clear for queenside castling.");
				return;
			}
		}

		// --- Move the King ---
		// Remove the king from its current position
		board.remove(king);
		// Calculate destination: e.g., "g1" for white kingside
		char kingDestFile = destination.charAt(0);
		int kingDestRank = Character.getNumericValue(destination.charAt(1));
		// Add the king at the new position
		ReturnPiece temp = new ReturnPiece();
		temp.pieceType = king.pieceType;
		temp.pieceFile = ReturnPiece.PieceFile.values()[kingDestFile - 'a'];
		temp.pieceRank = kingDestRank;
		board.add(temp);


		// --- Move the Rook ---
		if (destination.equals("g" + rank)) {  // Kingside castling
			// The rook moves from h1/h8 to f1/f8
			ReturnPiece rook = getPieceAt("h" + rank);
			if (rook != null) {
				board.remove(rook);
				// 'f' is the destination file for the rook

				temp.pieceType = rook.pieceType;
				temp.pieceFile = ReturnPiece.PieceFile.values()['f' - 'a'];
				temp.pieceRank = rank;
				board.add(temp);
			}
		} else if (destination.equals("c" + rank)) {  // Queenside castling
			// The rook moves from a1/a8 to d1/d8
			ReturnPiece rook = getPieceAt("a" + rank);
			if (rook != null) {
				board.remove(rook);
				// 'd' is the destination file for the rook
				temp.pieceType = rook.pieceType;
				temp.pieceFile = ReturnPiece.PieceFile.values()['d' - 'a'];
				temp.pieceRank = rank;
				board.add(temp);

			}
		}
	}*/


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
				System.out.println("player moved: " + move + "\n"); //           						 debug (just a reminder so i dont have to keep checking moves.txt)
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