package com.tiemens.tictactoe.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Represents the tic tac toe board full of CellValues
 * 
 * 0 1 2     or as row/col    [0,0]  [0,1]   [0,2]
 * 3 4 5                      [1,0]  [1,1]   [1,2]
 * 6 7 8                      [2,0]  [2,1]   [2,2]
 * 
 */
public class Board {
    private static int NUM_ROWS = 3;
    private static int NUM_COLS = 3;
    private static int BOARD_SIZE = NUM_ROWS * NUM_COLS;
    
    public static class RowCol {
        private final int row;
        private final int col;
        private final int index;
        private static final List<List<RowCol>> allRowCol = createAll(NUM_ROWS, NUM_COLS);
        private static final List<RowCol> allFlat = createFlatten(allRowCol);
        private static final Map<Integer, RowCol> index2Rowcol = createIndex2RowCol(allFlat);

        private static final List<RowCol> corners = List.of(RowCol.lookup(0,0), 
                                                            RowCol.lookup(0,2),
                                                            RowCol.lookup(2,0));

        private static final List<RowCol> sides = List.of(RowCol.lookup(0,1),
                                                          RowCol.lookup(1,0), 
                                                          RowCol.lookup(1,2),
                                                          RowCol.lookup(2,1));

        public static List<RowCol> getCorners() {
            return corners;
        }
        public static List<RowCol> getSides() {
            return sides;
        }
        public static RowCol lookup(int row, int col) {
            return allRowCol.get(row).get(col);
        }
        public static List<RowCol> getAllRowCol() {
            return allFlat;
        }

        private static List<RowCol> createFlatten(List<List<RowCol>> allrowcol2) {
            List<RowCol> rtn = new ArrayList<>();
        
            for (List<RowCol> row : allrowcol2) {
                rtn.addAll(row);
            }
            return rtn;
        }

        private static List<List<RowCol>> createAll(int numberRows, int numberCols) {
            List<List<RowCol>> ret = new ArrayList<>();
            for (int r = 0; r < numberRows; r++) {
                List<RowCol> row = new ArrayList<>();
                for (int c = 0; c < numberRows; c++) {
                    RowCol add = new RowCol(r, c);
                    row.add(add);
                }
                ret.add(row);
            }
            return ret;
        }
        private static Map<Integer, RowCol> createIndex2RowCol(List<RowCol> all) {
            Map<Integer, RowCol> rtn = new HashMap<>();
            for (RowCol rowcol : all) {
                rtn.put(rowcol.getIndex(), rowcol);
            }
            return rtn;
        }
        private RowCol(int row, int col) {
            this.row = row;
            this.col = col;
            this.index = row * NUM_ROWS + col;
        }
        public int getRow() {
            return row;
        }
        public int getCol() {
            return col;
        }
        public int getIndex() {
            return index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(col, row);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RowCol other = (RowCol) obj;
            return col == other.col && row == other.row;
        }
        public static RowCol lookupIndex(int index) {
            return index2Rowcol.get(index);
        }
        
        public String toString() {
            return "RowCol[" + row + "," + col + "]";
        }
    }

    private static List<Set<Integer>> allWinningIndexSets = createAllSetsWinningIndexes3x3();
    
    private List<CellValue> cellList;
    private List<Integer> turns = new ArrayList<>();
    
    public Board() {
        this(List.of(CellValue.EMPTY, CellValue.EMPTY, CellValue.EMPTY,
                     CellValue.EMPTY, CellValue.EMPTY, CellValue.EMPTY,
                     CellValue.EMPTY, CellValue.EMPTY, CellValue.EMPTY),
                new ArrayList<Integer>());
    }
    public Board(Board other) {
        this(other.cellList, other.turns);
    }
    public Board(List<CellValue> cellList, List<Integer> turns) {
        if (cellList.size() != BOARD_SIZE) {
            throw new RuntimeException("list size must be " + BOARD_SIZE + " but was " + cellList.size());
        }
        this.cellList = new ArrayList<>(cellList);
        this.turns = new ArrayList<>(turns);
    }
    
    
    
    public static List<Set<Integer>> getAllWinningIndexSets() {
        return allWinningIndexSets;
    }
    private static Set<Integer> createSet(int a, int b, int c) {
        return Set.of(a, b, c);
    }
    private static List<Set<Integer>> createAllSetsWinningIndexes3x3() {
        List<Set<Integer>> ret = new ArrayList<>();

        // horizontals
        ret.add( createSet(0, 1, 2) );
        ret.add( createSet(3, 4, 5) );
        ret.add( createSet(6, 7, 8) );
        // verticals
        ret.add( createSet(0, 3, 6) );
        ret.add( createSet(1, 4, 7) );
        ret.add( createSet(2, 5, 8) );
        // diagonals
        ret.add( createSet(0, 4, 8) );
        ret.add( createSet(2, 4, 6) );
        
        return ret;
    }

    public void clearAllCells() {
        for (int i = 0, n = cellList.size(); i < n; i++) {
            cellList.set(i, CellValue.EMPTY);
        }
    }
    public String toStringState() {
        return toStringState(" ", "\n");
    }
    public String toStringStateSingleLine() {
        return toStringState("", "");
    }
    public String toStringState(String separator, String newline) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < NUM_ROWS; r++) {
            String sep = "";
            for (int c = 0; c < NUM_COLS; c++) {
                sb.append(sep);
                sb.append(getCellRowCol(r, c));
                sep = separator;
            }
            sb.append(newline);
        }
        return sb.toString();
    }
    
    public String toStringTurnsSingleLine() {
        StringBuilder sb = new StringBuilder();
        for (Integer turn : turns) {
            sb.append("" + turn);
        }
        for (int i = 0, n = 9 - turns.size(); i < n; i++) {
            sb.append(".");
        }
        return sb.toString();
    }

    /**
     * 
     * @param positiveForPlayer
     * @return Array that matches Kaggle data format V1,V2,...V9,V10
     *          where V10 is "positive" or "negative"
     */
    public List<Object> toBoardStateKaggleCsv(CellValue positiveForPlayer) {
        if (positiveForPlayer == null) {
            positiveForPlayer = CellValue.X;
        }
    
        List<Object> rtn = new ArrayList<>();  // 9 CellValues then 1 String
        CellValue winner = getWinner();
        final String posnegnone;
        if (winner == null) {
            posnegnone = "none";
        } else {
            if (winner.equals(positiveForPlayer)) {
                posnegnone = "positive";
            } 
            else {
                posnegnone = "negative";
            }
        }
        
        for (int r = 0; r < NUM_ROWS; r++) {
            for (int c = 0; c < NUM_COLS; c++) {
                rtn.add( getCellRowCol( RowCol.lookup(r, c)) );
            }
        }
        rtn.add(posnegnone);
                
        return rtn;
    }   
                    
    public CellValue getCell(int index) {
        return cellList.get(index);
    }
    
    public void setCellRowCol(RowCol rowcol, CellValue cellValue) {
        int index = rowcol.getIndex();
        recordTurn(index);
        cellList.set(index, cellValue);
    }
    private void recordTurn(int index) {
        turns.add(index);
        
    }
    //public void setCell(int row, int col, CellValue cellvalue) {
    //    cellList[RowCol.lookup(row, col).getIndex()] = cellvalue;
    //}
    
    public CellValue getCell(int r, int c) {
        return getCellRowCol( RowCol.lookup(r, c) );
    }
        
    public CellValue getCellRowCol(RowCol rowcol) {
        return getCell( rowcol.getIndex() );
    }
    private CellValue getCellRowCol(int r, int c) {
        return getCellRowCol( RowCol.lookup(r, c) );
    }
    

    public boolean isCellEmpty(RowCol rowcol) {
        return getCellRowCol(rowcol).equals(CellValue.EMPTY);
    }

    /*package*/ int getOppositeIndex(int index) {
        if (index == 0) {
            return 2;
        }
        if (index == 1) {
            return 1;
        }
        if (index == 2) {
            return 0; 
        }
        throw new RuntimeException("opposite undefined for index " + index);
    }
    
    private RowCol getOppositeRowCol(RowCol rowcol) {
        return RowCol.lookup(getOppositeIndex(rowcol.getRow()), 
                             getOppositeIndex(rowcol.getCol()) );
    }

    public boolean compareMoves(RowCol rowcolOne, RowCol rowcolTwo) {
        if ((rowcolOne == null) || (rowcolTwo == null)) {
            throw new RuntimeException("both must be non-null");
        }
        return rowcolOne.equals(rowcolTwo);
    }

    public boolean findMatchingMove(RowCol findRowCol, List<RowCol> findInThisList) {
        for (RowCol target : findInThisList) {
            if (target.equals(findRowCol)) {
                return true;
            }
        }
        return false;
    }

    private void checkPlayerThrow(CellValue player) {
        if (player == null) {
            throw new RuntimeException("Player cannot be null");
        }
        if (player.isPlayer() == false) {
            throw new RuntimeException("Player must be a .player()");
        }
    }

    //# TODO: rename -- this returns [r,c] list of cells that are EMPTY
    public List<RowCol> listEmpty() {
        List<RowCol> rtn = new ArrayList<>();
        for (RowCol rowcol : RowCol.getAllRowCol()) {
            if (isCellEmpty(rowcol)) {
                rtn.add(rowcol);
            }
        }
        return rtn;
    }

    public RowCol computeMoveWinFor(CellValue player) {
        checkPlayerThrow(player);
        List<RowCol> wins = computeListMoveWinsFor(player);
        if (wins.size() > 0) {
            return wins.get(0);
        } else {
            return null;
        }
    }
    private List<RowCol> computeListMoveWinsFor(CellValue player) {
        checkPlayerThrow(player);
        List<RowCol> rtn = new ArrayList<>();
        List<RowCol> items = listEmpty();
        for (RowCol move : items) {
            if (doesMoveWinFor(player, move)) {
                rtn.add(move);
            }
        }
        return rtn;
    }       

    private boolean doesMoveWinFor(CellValue player, RowCol rowcol) {
        checkPlayerThrow(player);
        Board testboard = new Board(this);
        testboard.setCellRowCol(rowcol, player);
        List<RowCol> originalEmpty = listEmpty();
        List<RowCol> nowEmpty = listEmpty();
        CellValue winner = testboard.getWinner();
        boolean rtn = (player.equals(winner));
        return rtn;
    }

    //     # A fork move is a move that creates 2 winning moves    
    private RowCol computeMoveForksFor(CellValue player) {
        checkPlayerThrow(player);
        List<RowCol> moves = listMoveForksFor(player);
        if (moves.size() > 0) {
            return moves.get(0);
        } else {
            return null;
        }
    }
    
    private List<RowCol> listMoveForksFor(CellValue player) {
        checkPlayerThrow(player);
        List<RowCol> rtn = new ArrayList<>();
        List<RowCol> items = listEmpty();

        for (RowCol move : items) {
            Board testboard = new Board(this);
            testboard.setCellRowCol(move, player);
            List<RowCol> winningMoves = testboard.computeListMoveWinsFor(player);
            if (winningMoves.size() >= 2) {
                rtn.add(move);
            }
        }
        return rtn;
    }

    /** Note that "player" is a different sense for this method -
    *    i.e. this returns a move that will BLOCK player, not help player
    */
    private RowCol computeBlockForkMove(CellValue player, CellValue opponent) {
        checkPlayerThrow(player);
        RowCol rtn = null;
        List<RowCol> listForkMoves = listMoveForksFor(player);
        if (listForkMoves.size() > 0) {
            List<RowCol> items = listEmpty();
            for (RowCol moveRowColumn : items) {
                Board testboard = new Board(this);
                testboard.setCellRowCol(moveRowColumn, opponent);
                List<RowCol> opponentWinningMoves = testboard.computeListMoveWinsFor(opponent);
                for (RowCol check : opponentWinningMoves) {

                    if (findMatchingMove(check, listForkMoves)) {
                        //# if opponent winning move and player's for move are the same,
                        //#    then "moveRowColumn" does not work.
                        //#    skip it.
                    } else { 
                        if (rtn == null) {
                            rtn = moveRowColumn;
                            break;
                        }
                    }
                }
            }
        } else {
            // print(f"player {player} does not have a fork move")
        }

        return rtn;
    }

    public RowCol findFirstEmptyCorner() {
        return findFirstEmpty(RowCol.getCorners());
    }
    public RowCol findFirstEmptySide() {
        return findFirstEmpty(RowCol.getSides());
    }
    private RowCol findFirstEmpty(List<RowCol> moves) {
        for (RowCol move : moves) {
            if (isCellEmpty(move)) {
                return move;
            }
        }
        return null;
    }

    public RowCol findOppositeCornerMove(CellValue player) {
        //print(f" enter find_opposite_corner_move  player={player}  type={type(player)}")
        checkPlayerThrow(player);        
        RowCol rtn = null;
        for (RowCol move : RowCol.getCorners()) {
            //print(f"find_opp  move={move}")
            if (getCellRowCol(move).equals(player)) {
                //print(f"  matched player={player} at {move}")
                RowCol opposite = getOppositeRowCol(move);
                //print(f"  opposite is_empty {opposite} is {self.is_cell_empty(opposite[0], opposite[1])}")
                if (isCellEmpty(opposite)) {
                    rtn = opposite;
                }
            }
        }
        //print(f" exit find_opposite rtn={rtn}")
        return rtn;
    }

    /* default */ int countEmpty() {
        int count = 0;
        for (RowCol rowcol : RowCol.getAllRowCol()) {
            if (isCellEmpty(rowcol)) {
                count = count + 1;
            }
        }
        return count;
    }

    /**
     * @return CellValue X/O if three in a row somewhere, or
     *          null if there is no winner
     */
    public CellValue getWinner() {

        CellValue winner = null;
        List<Set<CellValue>> all_sets = new ArrayList<>();
        for (Set<Integer> index_set : getAllWinningIndexSets() ) {
            //all_sets.add( _create_set_from_array(index_set) )
            Set<CellValue> setValues = new HashSet<>();
            for (Integer index : index_set) {
                setValues.add( getCell(index) );
            }
            all_sets.add( setValues );
        } 
        
        for (Set<CellValue> candidateSet : all_sets) {
            if (! candidateSet.contains(CellValue.EMPTY)) {
                if (candidateSet.size() == 1) {
                    // when candidateSet is size 1 AND it is not EMPTY,
                    //   then either X or O has won.
                    //    so, "get the first/only" element of the set:
                    winner = candidateSet.iterator().next();
                    break;
                }
            }
        }

        return winner;
    }
    

    public void strategy_log(String msg) {
        if (false) {
            System.out.println(msg);
        }
    }

    //#// STRATEGY: "best"
    // Take the win, if available
    // Block the otherPlayer win, if the otherPlayer has one
    // Otherwise, take the first available EMPTY cell in ranked order of preference
    public RowCol strategyBest(CellValue usePlayer, CellValue otherPlayer) {
        Board board = this;
        
        RowCol winUse = board.computeMoveWinFor( usePlayer );
        if (winUse != null) {
            return winUse;
        }
        // log("No win for computer");

        RowCol winOther = board.computeMoveWinFor( otherPlayer );
        if (winOther != null) {
            return winOther;   // block the other player's win
        }
        // log("No win for human");

        RowCol rtn = null;
        List<RowCol> prefCells = List.of(
                   RowCol.lookup(1, 1),
                   RowCol.lookup(0, 0), RowCol.lookup(0, 2), RowCol.lookup(2, 0), RowCol.lookup(2, 2),
                   RowCol.lookup(0, 1),
                   RowCol.lookup(1, 0), RowCol.lookup(1, 2),
                   RowCol.lookup(2, 1));
        
        for (RowCol move : prefCells) {
            if (board.isCellEmpty(move)) {
                rtn =  move;
                break;
            }
        }
        return rtn;
    }  
    
    
    //#// STRATEGY: "perfect"
    //#//  pick cell that wins, else cell that blocks,
    //#//  else cell that forks, else cell that blocks fork,
    //#//  else middle, else opposite corner,
    //#//  else random corner, else random side
    public RowCol strategyPerfect(CellValue computerPlayer, CellValue humanPlayer) {
        RowCol winComputer = computeMoveWinFor( computerPlayer );
        if (winComputer != null) {
            return winComputer;
        }
        strategy_log("not wincomputer");

        RowCol winPlayer = computeMoveWinFor( humanPlayer );
        if (winPlayer != null) {
            return winPlayer;
        }
        strategy_log("not winplayer");

        RowCol forkComputer = computeMoveForksFor( computerPlayer );
        if (forkComputer != null) {
            return forkComputer;
        }
        strategy_log("not forkcomputer");

        RowCol forkPlayer = computeBlockForkMove( humanPlayer, computerPlayer );
        if (forkPlayer != null) {
            return forkPlayer;
        }
        strategy_log("not forkPlayer");

        RowCol center = RowCol.lookup(1, 1);
        if (isCellEmpty(center)) {
            return center;
        }

        strategy_log("not center");

        RowCol oppositeCorner = findOppositeCornerMove( humanPlayer );
        if (oppositeCorner != null) {
            return oppositeCorner;
        }
        strategy_log("not oppositecorner");

        RowCol randomCorner = findFirstEmptyCorner();
        if (randomCorner != null) {
            return randomCorner;
        }
        strategy_log("not randomcorner");

        RowCol randomSide = findFirstEmptySide();
        if (randomSide != null) {
            return randomSide;
        }
        strategy_log("not randomsize");

        strategy_log("ERROR: Programmer error");
        return null;
    }
    
    
    // Type-define wrappers for "String" - there are two "Key" string formats being used:
    // 1)  "board-state"  aka  xoxoxoxox
    // 2)  "turn-index"   aka  652183074
    // Having these classes means "Map<String, List<Game>>" turns into "Map<KeyBoardState, List<Game>>"
    //   so you can tell which kind of key was used
    // (Note that 95% of these classes is generated boiler-plate code.)
    
    public static class KeyBoardState implements Comparable<KeyBoardState> {
        private String key;

        public KeyBoardState(String key) {
            super();
            this.key = key;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            KeyBoardState other = (KeyBoardState) obj;
            return Objects.equals(key, other.key);
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int compareTo(KeyBoardState other) {
            return this.toString().compareTo(other.toString());
        }        
    } // KeyBoardState
    
    public static class KeyTurnIndex {
        private String key;

        public KeyTurnIndex(String key) {
            super();
            this.key = key;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            KeyTurnIndex other = (KeyTurnIndex) obj;
            return Objects.equals(key, other.key);
        }

        @Override
        public String toString() {
            return key;
        }        
    } // KeyBoardState
    
    public KeyBoardState getKeyBoardState() {
        StringBuilder sb = new StringBuilder();
        for (CellValue cellValue: cellList) {
            sb.append(cellValue.getSymbol());
        }
        return new KeyBoardState(sb.toString());
    }
    public KeyTurnIndex getKeyTurnIndex() {
        StringBuilder sb = new StringBuilder();
        for (Integer turn : turns) {
            sb.append("" + turn);
        }
        return new KeyTurnIndex(sb.toString());        
    }
}

    