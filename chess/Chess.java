// Will Tran & Jack Loyd


package chess;

import chess.ReturnPiece;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chess {

	enum Player {white, black}

	private static Player currentPlayer;
	private static ArrayList<ReturnPiece> board;
	private static ReturnPiece enPassant = null; // If a pawn is ever moved from the start row, record it so the next move can check for an En Passant
	private static boolean recentDoubleMove = false;
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
		boolean isIllegalMove = false;

		System.out.println(currentPlayer + "'s turn : " + parts[0] + " to " + parts[1]);
		if (move.equals("resign")) {
			result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.RESIGN_BLACK_WINS : ReturnPlay.Message.RESIGN_WHITE_WINS;
		}


		else if (parts.length == 2 || (parts.length == 3 && (parts[2].equals("draw?")) || parts[2].length() == 1)) {
			ReturnPiece piece = getPieceAt(parts[0]);

			if (piece == null || !isCurrentPlayerPiece(piece) || !isValidMove(piece, parts[1], false)) {
				//System.out.println("first illegal move"); //debug
				//System.out.println("piece == null = "  + (piece == null) + "\n!isCurrentPlayerPiece(piece) = " + (!isCurrentPlayerPiece(piece)) + "\n!isValidMove(piece, parts[1], false) = " + (!isValidMove(piece, parts[1], false)));
				result.message = ReturnPlay.Message.ILLEGAL_MOVE;
				isIllegalMove = true;
			}

			// check if move will put moved king in check
			else if (piece.pieceType.toString().charAt(1) == 'K') {
				char newFile = parts[1].charAt(0);
				int newRank = Character.getNumericValue(parts[1].charAt(1));

				ReturnPiece.PieceType kingType = (currentPlayer == Player.white) ? ReturnPiece.PieceType.WK : ReturnPiece.PieceType.BK;

				ReturnPiece king = new ReturnPiece();
				king.pieceType = kingType;
				king.pieceFile = ReturnPiece.PieceFile.values()[newFile - 'a'];
				king.pieceRank = newRank;

				if (isCheck(king)) {
					isIllegalMove = true;
					result.message = ReturnPlay.Message.ILLEGAL_MOVE;
				}
			}


			// Handle promotion
			else if (parts.length == 3 && parts[2].length() == 1) {
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
				movePiece(tempPiece, parts[1], true);
				board.remove(piece);
			} else {
				// Move the piece
				movePiece(piece, parts[1], true);
			}

			if(!isIllegalMove) {
				// Switch turn after move
				switchPlayer();
			}


			// Check if the opponent is in check
			ReturnPiece king = findKing();
			if (isCheck(king)) {
				// Check if the threatened King has any valid escape moves
				if (isCheckMate(king)) {
					result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.CHECKMATE_BLACK_WINS : ReturnPlay.Message.CHECKMATE_WHITE_WINS;
				} else {
					result.message = ReturnPlay.Message.CHECK;
				}
			}

			// Handle draw request
			if (parts.length == 3 && parts[2].equals("draw?")) {
				result.message = ReturnPlay.Message.DRAW;
			}


		}
		else {
			System.out.println("else illegal move");
			result.message = ReturnPlay.Message.ILLEGAL_MOVE;
		}
		result.piecesOnBoard = new ArrayList<>(board);
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
		//System.out.println("piece sent to isValidMove = " + piece); //debug

		switch (piece.pieceType) {
			case WP: // White Pawn
				if (fileDiff == 0 && destRank == piece.pieceRank + 1 && targetPiece == null) {
					return true; // Normal move forward
				}
				if (fileDiff == 1 && destRank == piece.pieceRank + 1 && recentDoubleMove) {
//					System.out.println(currentPlayer + " diagonal capture with pawn: " + piece); //								debug
					if (enPassant != null && enPassant.pieceFile.ordinal() == destFile) {
						board.remove(getPieceAt(destination.substring(0, 1) + (destRank - 1)));
						enPassant = null;
//						System.out.println("\n\n en passant!!!!!!!!\n\n"); //							debug
						return true; // En Passant capture
					}
					return false;
				}
				if (fileDiff == 1 && destRank == piece.pieceRank + 1 && isCapturing) {
//					System.out.println(currentPlayer + " diagonal capture with pawn: " + piece); //								debug
					return true; // Capturing diagonally
				}
				if (fileDiff == 0 && piece.pieceRank == 2 && destRank == 4 && targetPiece == null) {
//					System.out.println(currentPlayer + " double moved pawn: " + piece); //								debug
					enPassant = piece;
					recentDoubleMove = true;
					return true; // Double move from starting position
				}
				return false;
			case BP: // Black Pawn
				if (fileDiff == 0 && destRank == piece.pieceRank - 1 && targetPiece == null) {
					return true; // Normal move forward
				}
				if (recentDoubleMove && fileDiff == 1 && destRank == piece.pieceRank - 1) {
//					System.out.println(currentPlayer + " diagonal capture with pawn: " + piece); //								debug
					if (enPassant != null && enPassant.pieceFile.ordinal() == destFile) {
//						System.out.println("\n\n en passant!!!!!!!!\n\n"); //							debug
						board.remove(getPieceAt(destination.substring(0, 1) + (destRank + 1)));
						enPassant = null;
						return true; // En Passant capture
					}
					return false; // Capturing diagonally
				}
				if (fileDiff == 1 && destRank == piece.pieceRank - 1 && isCapturing) {
//					System.out.println(currentPlayer + " diagonal capture with pawn: " + piece); //								debug

					return true; // Capturing diagonally
				}
				if (fileDiff == 0 && piece.pieceRank == 7 && destRank == 5 && targetPiece == null) {
//					System.out.println(currentPlayer + " double moved pawn: " + piece); //								debug
					enPassant = piece;
					return true; // Double move from starting position
				}
				return false;
			case WR:
				if (fileDiff == 0) { // Moving vertically
					return isPathClear(piece, destination, true) && (targetPiece == null || isCapturing);
				}
				if (rankDiff == 0) { // Moving horizontally
					return isPathClear(piece, destination, false) && (targetPiece == null || isCapturing);
				}
			case BR: // Rook
				if (fileDiff == 0) { // Moving vertically
					return isPathClear(piece, destination, true) && (targetPiece == null || isCapturing);
				}
				if (rankDiff == 0) { // Moving horizontally
					return isPathClear(piece, destination, false) && (targetPiece == null || isCapturing);
				}
				return false;
			case WN:
			case BN: // Knight
				return (fileDiff == 2 && rankDiff == 1) || (fileDiff == 1 && rankDiff == 2);
			case WB:
			case BB: // Bishop
				return (fileDiff == rankDiff) && isPathDiagonalClear(piece, destination) && (targetPiece == null || isCapturing);
			case WQ:
			case BQ: // Queen
				if (fileDiff == rankDiff) { // Moving diagonally
					return isPathDiagonalClear(piece, destination) && (targetPiece == null || isCapturing);
				}
				if (fileDiff == 0 || rankDiff == 0) { // Moving straight
					return isPathClear(piece, destination, fileDiff == 0) && (targetPiece == null || isCapturing);
				}
				return false;
			case WK:
				// check for castle
				if (canWhiteCastleQueen && destination.equalsIgnoreCase("c1")) {
					return isPathClear(piece, "b1", false); // The white king is able to castle queenside

				}
				if (canWhiteCastleKing && destination.equalsIgnoreCase("g1")) {
					return isPathClear(piece, "g1", false); // The white king is able to castle queenside
				}






				return fileDiff <= 1 && rankDiff <= 1 && (targetPiece == null || isCapturing);
			case BK: // King
				if (canBlackCastleQueen && destination.equalsIgnoreCase("c8")) {
					return isPathClear(piece, "b8", false); // The black king is able to castle queenside
				}
				if (canBlackCastleKing && destination.equalsIgnoreCase("g8")) {
					return isPathClear(piece, "g8", false); // The black king is able to castle queenside
				}

				return fileDiff <= 1 && rankDiff <= 1 && (targetPiece == null || isCapturing);
			default:
				return false;
		}
	}


	private static void movePiece(ReturnPiece piece, String destination, boolean isRookCastleException) {
		char destFile = destination.charAt(0);
		int destRank = Character.getNumericValue(destination.charAt(1));
		int fileDiff = Math.abs(destFile - piece.pieceFile.ordinal());



		//check for special cases
		System.out.println("piecetype = " + piece.pieceType);
		switch (piece.pieceType) {
			case WR:
				//disable white castling on the side of the rook (either a or h)
				if (isRookCastleException && canWhiteCastleQueen && piece.toString().charAt(0) - 'a' == 0) {
					canWhiteCastleQueen = false;
				}
				if (isRookCastleException && canWhiteCastleKing && piece.toString().charAt(0) - 'a' == 7) {
					canWhiteCastleKing = false;
				}
				break;
			case BR: // Rook
				//disable black castling on the side of the rook
				if (isRookCastleException && canBlackCastleKing && piece.toString().charAt(0) - 'a' == 0) {
					canBlackCastleKing = false;
				}
				if (isRookCastleException && canBlackCastleQueen && piece.toString().charAt(0) - 'a' == 7) {
					canBlackCastleQueen = false;
				}
				break;
			case WK:
				if (canWhiteCastleQueen && destination.equals("c1")) {
					// Move the rook for the castle
					ReturnPiece rook = getPieceAt("a1");
					board.remove(rook);
					ReturnPiece tempRook = new ReturnPiece();
					tempRook.pieceType = ReturnPiece.PieceType.WR;
					tempRook.pieceFile = ReturnPiece.PieceFile.valueOf("c");
					tempRook.pieceRank = destRank;
					board.add(tempRook);
				}
				System.out.println("canWhiteCastleKing = " + canWhiteCastleKing + "\ndestination.equals(\"g1\") = " + destination.equals("g1"));
				if (canWhiteCastleKing && destination.equals("g1")) {
					System.out.println("castle successful");
					// Move the rook for the castle
					ReturnPiece rook = getPieceAt("h1");
					board.remove(rook);
					ReturnPiece tempRook = new ReturnPiece();
					tempRook.pieceType = ReturnPiece.PieceType.WR;
					tempRook.pieceFile = ReturnPiece.PieceFile.valueOf("f");
					tempRook.pieceRank = destRank;
					board.add(tempRook);
				}
				//disable white castling
				canWhiteCastleQueen = false;
				canWhiteCastleKing = false;
			case BK: // King
				// check for castle
				if (canBlackCastleQueen && destination.equals("c8")) {
					// Move the rook for the castle
					ReturnPiece rook = getPieceAt("a8");
					board.remove(rook);
					ReturnPiece tempRook = new ReturnPiece();
					tempRook.pieceType = ReturnPiece.PieceType.WR;
					tempRook.pieceFile = ReturnPiece.PieceFile.valueOf("c");
					tempRook.pieceRank = destRank;
					board.add(tempRook);
				}
				if (canBlackCastleKing && destination.equals("g8")) {
					// Move the rook for the castle
					ReturnPiece rook = getPieceAt("h8");
					board.remove(rook);
					ReturnPiece tempRook = new ReturnPiece();
					tempRook.pieceType = ReturnPiece.PieceType.WR;
					tempRook.pieceFile = ReturnPiece.PieceFile.valueOf("f");
					tempRook.pieceRank = destRank;
					board.add(tempRook);
				}
				canBlackCastleQueen = false;
				canBlackCastleKing = false;

				break;
		}

		// Remove the opponent's piece if it exists at the destination
		ReturnPiece targetPiece = getPieceAt(destination);
		if (targetPiece != null && !isCurrentPlayerPiece(targetPiece)) {
			board.remove(targetPiece);
		}

		// Remove the moving piece from its current position
		board.remove(piece);

		// Add the piece to the new position
		ReturnPiece temp = new ReturnPiece();
		temp.pieceType = piece.pieceType;
		temp.pieceFile = ReturnPiece.PieceFile.values()[destFile - 'a'];
		temp.pieceRank = destRank;
		board.add(temp);

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



}