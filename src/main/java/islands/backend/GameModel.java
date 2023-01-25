package islands.backend;


/**
 * Class to handle backend of Hex game.
 *
 * @author Jonathon Meney
 * @version 1.0, 01/25/23
 */
public class GameModel {
    public static final boolean WHITE = true;
    public static final boolean BLACK = false;

    /**
     * The X by X board the game is being played on.
     */
    private final Board board;

    /**
     * The number of moves the white player has made.
     */
    private int whiteMovesMade;

    /**
     * The number of moves the black player has made.
     */
    private int blackMovesMade;

    /**
     * Tracks if white has played on the top edge of the board yet.
     */
    private boolean whitePlayedOnTop;

    /**
     * Tracks if white has played on the bottom edge of the board yet.
     */
    private boolean whitePlayedOnBottom;

    /**
     * Tracks if black has played on the left edge of the board yet.
     */
    private boolean blackPlayedOnLeft;

    /**
     * Tracks if black has played on the right edge of the board yet.
     */
    private boolean blackPlayedOnRight;

    /**
     * Construct a square board with given side length to track game.
     *
     * @param sideLength the width and height of the square board
     * @see Board
     */
    public GameModel(int sideLength) {
        board = new Board(sideLength);
    }

    /**
     * Determine if a play can be made at a specific row and column.
     *
     * @param row the row the hex is in
     * @param col the column the hex is in
     * @return true if the position is empty, false otherwise
     * @throws IllegalArgumentException if row and column given are invalid
     */
    public boolean canPlay(int row, int col) {
        // check bounds of the board
        if ((row >= board.getBoardSideLength()) || (col >= board.getBoardSideLength()) || (row < 0) || (col < 0)) {
            throw new IllegalArgumentException();
        }
        // all cells default to grey or 0
        // if -1 its taken by black
        // if 1 its taken by white
        return board.getHexColor(row, col) == 0;
    }

    /**
     * Make a play if possible and determine if the game is over yet.
     *
     * @param row the row where a move is attempted
     * @param col the column where a move is attempted
     * @param clr true for WHITE and false for BLACK
     * @return true if the game is over, false otherwise
     * @throws IllegalArgumentException if row and column given are invalid
     */
    public boolean makePlay(int row, int col, boolean clr) {
        if (canPlay(row, col)) {
            // check if white has played on the top or bottom yet
            // and see if the current move is on the top or bottom
            if (clr && (!whitePlayedOnTop || !whitePlayedOnBottom) ) {
                if (row == 0) {
                    whitePlayedOnTop = true;
                } else if (row == board.getBoardSideLength() -1) {
                    whitePlayedOnBottom = true;
                }
            }
            // check if black has played on the left or right yet
            // and see if the current move is on the left or right
            else if (!blackPlayedOnLeft || !blackPlayedOnRight) {
                if (col == 0) {
                    blackPlayedOnLeft = true;
                } else if (col == board.getBoardSideLength() - 1) {
                    blackPlayedOnRight = true;
                }
            }

            // track how many moves each color has made
            // to determine if they have enough moves to cross the board
            if (clr){
                whiteMovesMade += 1;
            } else {
                blackMovesMade += 1;
            }

            // make the move
            board.setHexColor(row, col, clr);

            // WHITE CHECK
            // only check for a win if white has a move on the top and the bottom of the board
            // and if they have the minimum number of moves to go from one side to the other
            if (clr && whitePlayedOnTop && whitePlayedOnBottom && whiteMovesMade >= board.getBoardSideLength()) {

                // check if the current move is connected to any hexes in the top row
                for (int i = 0; i < board.getBoardSideLength(); i++) {
                    if (board.connected(i, board.getHexIndex(row, col))) {

                        // check if the current move is also connected to any hexes in the bottom row
                        for (int j = (board.getBoardSideLength() - 1) * board.getBoardSideLength(); j < board.getBoardSideLength() * board.getBoardSideLength(); j++) {
                            if (board.connected(board.getHexIndex(row, col), j)) {
                                // if the current move was connected to top and bottom a win has happened
                                return true;
                            }
                        }
                    }
                }
            }

            // BLACK CHECK
            // only check for a win if black has a move on the left and the right of the board
            // and if they have the minimum number of moves to go from one side to the other
            else if (blackPlayedOnLeft && blackPlayedOnRight && blackMovesMade >= board.getBoardSideLength()) {

                // check if the current move is connected to any hexes in the left column
                for (int i = 0; i < board.getBoardSideLength() * board.getBoardSideLength(); i += board.getBoardSideLength()) {
                    if (board.connected(i, board.getHexIndex(row, col))) {

                        // check if the current move is also connected to any hexes in the right column
                        for (int j = board.getBoardSideLength() - 1; j < board.getBoardSideLength() * board.getBoardSideLength(); j += board.getBoardSideLength()) {
                            if (board.connected(board.getHexIndex(row, col), j)) {
                                // if the current move was connected to left and right a win has happened
                                return true;
                            }
                        }
                    }
                }
            }

        }
        // if we can't play or no wins occurred return false
        return false;
    }

    /**
     * Return the score for white.
     *
     * @return white's score
     */
    public int whiteScore() {
        return board.getWhiteIslands();
    }

    /**
     * Return the score for black.
     *
     * @return black's score
     */
    public int blackScore() {
        return board.getBlackIslands();
    }
}
