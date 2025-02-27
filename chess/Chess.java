package chess;

public class Chess {

        enum Player { white, black }
		static Board board;
    
	/**
	 * Plays the next move for whichever player has the turn.
	 * 
	 * @param move String for next move, e.g. "a2 a3"
	 * 
	 * @return A ReturnPlay instance that contains the result of the move.
	 *         See the section "The Chess class" in the assignment description for details of
	 *         the contents of the returned ReturnPlay instance.
	 */
	// public static ReturnPlay play(String move) {

	// 	/* FILL IN THIS METHOD */
		
		
	// 	/* FOLLOWING LINE IS A PLACEHOLDER TO MAKE COMPILER HAPPY */
	// 	/* WHEN YOU FILL IN THIS METHOD, YOU NEED TO RETURN A ReturnPlay OBJECT */
	// 	return null;
	// }
	
	
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void main(String[] args) {
		
		/* FILL IN THIS METHOD */
		board = new Board();  // Instantiate a new board
        board.initializeBoard(); // Initialize the pieces on the board
        board.printBoard(); // Print the board to verify initialization
	}
	public static void initializeBoard(){

	}
	
}
