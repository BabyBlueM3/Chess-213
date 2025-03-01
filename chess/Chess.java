package chess;

public class Chess {

        enum Player { white, black }
		public static Board board;
		public static Player currentPlayer;

    
	/**
	 * Plays the next move for whichever player has the turn.
	 * 
	 * @param move String for next move, e.g. "a2 a3"
	 * 
	 * @return A ReturnPlay instance that contains the result of the move.
	 *         See the section "The Chess class" in the assignment description for details of
	 *         the contents of the returned ReturnPlay instance.
	 */
	public static ReturnPlay play(String move) {
		move = move.trim();
		String[] parts = move.split(" ");
		ReturnPlay result = new ReturnPlay();
	
		if (move.equals("resign")) {
			result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.RESIGN_BLACK_WINS : ReturnPlay.Message.RESIGN_WHITE_WINS;
			return result;
		}
	
		if (parts.length < 2 || parts.length > 3) {
			result.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return result;
		}
	
		int startX = parts[0].charAt(1) - '1';
		int startY = parts[0].charAt(0) - 'a';
		int endX = parts[1].charAt(1) - '1';
		int endY = parts[1].charAt(0) - 'a';
	
		Piece piece = board.getPiece(startX, startY);
	
		if (piece == null || piece.getPlayer() != currentPlayer || !board.isValidMove(startX, startY, endX, endY, currentPlayer)) {
			result.message = ReturnPlay.Message.ILLEGAL_MOVE;
			return result;
		}
	
		// Move the piece
		board.movePiece(startX, startY, endX, endY);
	
		// Check for checkmate
		if (board.isCheckmate(currentPlayer == Player.white ? Player.black : Player.white)) {
			result.message = (currentPlayer == Player.white) ? ReturnPlay.Message.CHECKMATE_WHITE_WINS : ReturnPlay.Message.CHECKMATE_BLACK_WINS;
			return result;
		}
	
		// Check if opponent is in check
		if (board.isCheck(currentPlayer == Player.white ? Player.black : Player.white)) {
			result.message = ReturnPlay.Message.CHECK;
		}
	
		// Handle draw request
		if (parts.length == 3 && parts[2].equals("draw?")) {
			result.message = ReturnPlay.Message.DRAW;
		}
	
		// Switch turn
		switchPlayer();
	
		return result;
	}
	
	public static void initializeBoard() {
		
		/* FILL IN THIS METHOD */
		board = new Board();  // Instantiate a new board
        board.initializeBoard(); // Initialize the pieces on the board
        board.printBoard(); // Print the board to verify initialization
	}

	public boolean isValidMove(int startX, int startY, int endX, int endY, Player currentPlayer) {
		Piece piece = board[startX][startY];
	
		if (piece == null) {
			return false; // No piece at the starting position
		}
	
		if (piece.getPlayer() != currentPlayer) {
			return false; // Player can only move their own pieces
		}
	
		if (!piece.isValidMove(startX, startY, endX, endY, this)) {
			return false; // Check piece-specific movement rules
		}
	
		if (isPathObstructed(startX, startY, endX, endY)) {
			return false; // Ensure no pieces are blocking the path (except knights)
		}
	
		Piece destinationPiece = board[endX][endY];
		if (destinationPiece != null && destinationPiece.getPlayer() == currentPlayer) {
			return false; // Can't capture own piece
		}
	
		return true;
	}
	



	public static void switchPlayer() {
		currentPlayer = (currentPlayer == Player.white) ? Player.black : Player.white;
	}



	
	 /**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
        currentPlayer = Player.white;
        initializeBoard();
	}
	
	public static void main(String[] args) {
		initializeBoard();
	}
	
}
