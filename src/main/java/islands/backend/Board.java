package islands.backend;


/**
 * Class to manage Hex board. Implements Weighted Quick Union with Path Compression.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * @author Jonathon Meney
 * @version 1.0, 01/25/23
 * @see <a href="https://algs4.cs.princeton.edu/15uf/WeightedQuickUnionPathCompressionUF.java.html">Weighted Quick Union with Path Compression</a>
 */
public class Board {
    /**
     * Tracks each hex on the board, and updates when hexes are unionised.
     */
    private final int[] hexes;

    /**
     * Tracks the size (weight) of each group, used for weighted union.
     */
    private final int[] size;

    /**
     * Stores the color of a particular hex, -1 is black, 0 is grey/empty, 1 is white.
     */
    private final int[] color;

    /**
     * The length and width of the board.
     */
    private final int boardSideLength;

    /**
     * The number of islands white has.
     */
    private int whiteIslands;

    /**
     * The number of islands black has.
     */
    private int blackIslands;

    /**
     * The number of groups that have been unionised.
     */
    private int count;

    /**
     * Initialize all fields, and fill board with all unique roots and their weights to 1.
     *
     * @param boardSideLength the width and height of the board
     */
    public Board(int boardSideLength) {
        this.boardSideLength = boardSideLength;
        count = boardSideLength * boardSideLength;  // total hexes on board

        hexes = new int[count];
        size = new int[count];
        color = new int[count];

        // root everything to itself and all weights to 1
        for (int i = 0; i < count; i++) {
            hexes[i] = i;
            size[i] = 1;
        }
    }

    /**
     * Union 2 hexes together.
     *
     * @param hex1 hex to be unionised to hex2
     * @param hex2 hex to be unionised to hex1
     */
    public void union(int hex1, int hex2) {
        int i = find(hex1);
        int j = find(hex2);

        // if they're the same they are already unionised
        if (i == j) {
            return;
        }

        // union the smaller tree to the bigger one
        if (size[i] < size[j]) {
            hexes[i] = j;
            size[j] += size[i];
        } else {
            hexes[j] = i;
            size[i] += size[j];
        }

        // everytime we union we lose a unique group
        count--;
    }

    /**
     * Returns if 2 hexes are connected to each other or not.
     *
     * @param hex1 a hex
     * @param hex2 another hex
     * @return true if they are connected, false otherwise
     */
    public boolean connected(int hex1, int hex2) {
        return find(hex1) == find(hex2);
    }

    /**
     * Returns the root of a hex.
     *
     * @param hex a hex
     * @return the root of the given hex
     */
    public int find(int hex) {
        while (hex != hexes[hex]) {
            hexes[hex] = hexes[hexes[hex]]; // path compression
            hex = hexes[hex];
        }
        return hex;
    }

    /**
     * Returns the current number of groups after some number of unions.
     *
     * @return the current number of groups
     */
    public int count(){
        return count;
    }

    /**
     * Unions all surrounding hexes which match the color of the current move.
     *
     * @param row the row of the current move
     * @param col the column of the current move
     * @param clr the color of the current move
     */
    private void unionNeighbors(int row, int col, int clr) {
        // hex to find the neighbors of
        int hexIndex = getHexIndex(row, col);

        // tracks number of neighbors we are going to union to
        int unions = 0;

        // tracks the indices of hexes we will need to union to
        // initialized to -1 as 0 would be the first cell
        int[] unionHexes = {-1, -1, -1, -1, -1, -1};

        // the following 6 if statements check for a neighboring hex of matching color
        // neighbors are:
        // the hexes to the left and right
        // the hexes above and below
        // the hex above and to the left
        // and the hex below and to the right
        if (col-1 >= 0 && hexIndex-1 >= 0 && color[hexIndex-1] == clr){
            unionHexes[unions] = hexIndex-1;
            unions++;
        }
        if (col+1 < boardSideLength && hexIndex+1 < color.length && color[hexIndex+1] == clr) {
            unionHexes[unions] = hexIndex+1;
            unions++;
        }
        if (row-1 >= 0 && hexIndex- boardSideLength >= 0 && color[hexIndex- boardSideLength]  == clr) {
            unionHexes[unions] = hexIndex- boardSideLength;
            unions++;
        }
        if (row+1 < boardSideLength && hexIndex+ boardSideLength < color.length && color[hexIndex+ boardSideLength]  == clr) {
            unionHexes[unions] = hexIndex+ boardSideLength;
            unions++;
        }
        if (col-1 >= 0 && row-1 >= 0 && hexIndex- boardSideLength -1 >= 0 && color[hexIndex- boardSideLength -1]  == clr) {
            unionHexes[unions] = hexIndex- boardSideLength -1;
            unions++;
        }
        if (col+1 < boardSideLength && row+1 < boardSideLength && hexIndex+ boardSideLength +1 < color.length && color[hexIndex+ boardSideLength +1]  == clr) {
            unionHexes[unions] = hexIndex+ boardSideLength +1;
            unions++;
        }

        // score only needs to be updated if doing less than 5 unions and not exactly 1 union, see updateScores
        if (unions !=1 && unions < 5){
            // counts the number of connections between neighbors before unionising to them
            int connections = 0;

            outerLoop: for (int i = 0; i < unions; i++) {
                for (int j = i+1; j < unions; j++) {
                    if (unionHexes[i] != -1 && unionHexes[j] != -1) {
                        if (connected(unionHexes[i], unionHexes[j])) {
                            connections += 1;
                        }
                    } else {
                        break outerLoop;
                    }
                }
            }

            updateScores(unions, connections, clr);
        }

        // union to all neighbors
        for (int hex : unionHexes) {
            if (hex != -1) {
                union(hexIndex, hex);
            }
        }
    }

    /**
     * Calculate the score for a color based on the number of connected hexes being unionised to.
     *
     * @param unions the number of unions to neighboring hexes
     * @param connections the number of connections between the hexes being unionised to
     * @param clr the color of the hex being unionised
     */
    private void updateScores(int unions, int connections, int clr) {
        int islands;

        // get current islands of color being unionised
        if (clr == 1) {
            islands = getWhiteIslands();
        } else {
            islands = getBlackIslands();
        }

        // if 1 union, 5 unions, or 6 unions occur
        // the number of islands will not change ever
        // 1 union is just growing the island by a hex
        // 5 or 6 unions will always surround a hex fully just growing the island by a hex

        // the case of 0, 2, 3, and 4 unions is below
        switch (unions) {
            // 0 unions, new island
            case 0 -> islands += 1;

            // 2 unions none connected (0 connections) -1 island
            // 2 unions 2 connected(1 connections) +0 islands
            case 2 -> {
                if (connections == 0) islands -= 1;
            }

            // 3 unions none connected (0 connections) -2 islands
            // 3 unions 2 connected (1 connection) -1 island
            // 3 unions 3 connected (3 connections) +0 islands
            case 3 -> {
                if (connections == 0) {
                    islands -= 2;
                } else if (connections == 1) {
                    islands -= 1;
                }
            }

            // 4 unions 2 connected (2 connections) -1 island
            // 4 unions 3 connected (3 connections) -1 island
            // 4 unions 4 connected (6 connections) +0 islands
            case 4 -> {
                if (connections == 2 || connections == 3) islands -= 1;
            }
        }

        // set new island count of the color being unionised
        if (clr == 1) {
            setWhiteIslands(islands);
        } else {
            setBlackIslands(islands);
        }
    }

    /**
     * Gets the index of a hex in the 1d hexes array.
     *
     * @param row the row the given hex is in
     * @param col the column th given hex is in
     * @return the index of the hex
     */
    public int getHexIndex(int row, int col) {
        // simple formula for getting the index of a 1d array
        // as if it were a 2d array
        return row * boardSideLength + col;
    }

    /**
     * Returns the color of a given hex.
     *
     * @param row the row the given hex is in
     * @param col the column the given hex is in
     * @return the color of the requested hex
     */
    public int getHexColor(int row, int col) {
        int hex = getHexIndex(row, col);
        return color[hex];
    }

    /**
     * Sets the color of a specific hex to a given color, and unions it to its neighbors.
     *
     * @param row the row the given hex is in
     * @param col the column the given hex is in
     * @param clr the color to make the given hex
     */
    public void setHexColor(int row, int col, boolean clr) {
        int hexIndex = getHexIndex(row, col);

        // white
        if (clr) {
            color[hexIndex] = 1;
            unionNeighbors(row, col, 1);
        }
        // black
        else {
            color[hexIndex] = -1;
            unionNeighbors(row, col, -1);
        }

    }

    /**
     * Returns the number of islands white has.
     *
     * @return the number of islands white has
     */
    public int getWhiteIslands() {
        return whiteIslands;
    }

    /**
     * Sets the number of islands white has.
     *
     * @param whiteIslands the new number of white islands
     */
    public void setWhiteIslands(int whiteIslands) {
        this.whiteIslands = whiteIslands;
    }

    /**
     * Returns the number of islands black has.
     *
     * @return the number of islands black has
     */
    public int getBlackIslands() {
        return blackIslands;
    }

    /**
     * Sets the number of islands black has.
     *
     * @param blackIslands the new number of black islands
     */
    public void setBlackIslands(int blackIslands) {
        this.blackIslands = blackIslands;
    }

    /**
     * Returns the side length of the board.
     *
     * @return the side length of the board
     */
    public int getBoardSideLength() {
        return boardSideLength;
    }
}
