package com.tiemens.tictactoe.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tiemens.tictactoe.model.Board.RowCol;


class BoardTest {

    @Test
    void testEmptyWinner() {
        Board board = new Board();
        CellValue winner = board.getWinner();
        assertNull(winner);
    }
    @Test
    void testOpposite() {
        Board board = new Board();
        assertTrue(board.isCellEmpty( RowCol.lookup(0,0)));
        assertEquals(0, board.getOppositeIndex(2));
        assertEquals(1, board.getOppositeIndex(1));
        assertEquals(0, board.getOppositeIndex(2));        
    }

    @Test
    void  testEmptyCorner() {
        Board board = new Board();
        assertEquals( RowCol.lookup(0,0), board.findFirstEmptyCorner());
        board.setCellRowCol(RowCol.lookup(0,0), CellValue.X);
        assertEquals(RowCol.lookup(0,2), board.findFirstEmptyCorner());
        assertEquals(RowCol.lookup(2,2), board.findOppositeCornerMove(CellValue.X));
    }
    
    public static RowCol rc(int r, int c) {
        return RowCol.lookup(r, c);
    }
    @Test
    void testEmptySide() {
        Board board = new Board();
        assertEquals( rc(0,1), board.findFirstEmptySide());
        board.setCellRowCol( rc(0,1), CellValue.X);
        assertEquals( rc(1,0), board.findFirstEmptySide());
        board.setCellRowCol( rc(1,0), CellValue.X);
        assertEquals( rc(1,2),  board.findFirstEmptySide());
        board.setCellRowCol( rc(1, 2), CellValue.X);
        assertEquals( rc(2,1),  board.findFirstEmptySide());
    }
    
    @Test
    void testSamemove() {
        Board board = new Board();
        assertEquals(false,  board.compareMoves( rc(0,0), rc(1,1) ));
        assertEquals(true,   board.compareMoves( rc(0,0), rc(0,0) ));
        assertEquals(true,   board.compareMoves( rc(1,1), rc(1,1) ));
    }
    
    @Test
    void testFindmove() {
        Board board = new Board();
        List<RowCol> testlist = List.of(rc(0,1), rc(1,2));
        assertEquals(false, board.findMatchingMove( rc(0, 0), testlist));
        assertEquals(true,  board.findMatchingMove( rc(0, 1), testlist));
        assertEquals(false, board.findMatchingMove( rc(0, 2), testlist));
        assertEquals(true,  board.findMatchingMove( rc(1, 2), testlist));
    }
    
    @Test
    void testPerfectBlockers() {
        Board board = new Board();
        board.setCellRowCol(rc(0, 2), CellValue.O);
        board.setCellRowCol(rc(1, 1), CellValue.X);
//##        board.set_cell(2, 0, O)  ## TODO: previous test set both corners
        //print(f"perfectblocker start")
        assertEquals( rc(2, 0),  board.findOppositeCornerMove(CellValue.O));
        //# very hard to get strategy to agree:
        //# assert board.strategy_perfect(X, O) == [0, 2]
    }

    // # end of original unit tests
    // # start of symmetry unit tests

    //
    // list comprehension
    // List<String> board_array_quotes = board_array.stream().map(quote + Object::toString + quote).collect(Collectors.toList());
    // works List<String> board_array_quotes = board_array.stream().map(Object::toString).collect(Collectors.toList());            

    
    /**
    def test_indexflip(self):
        ninesym = NineSymmetry()
        orig = [0,1,2,3,4,5,6,7,8]
        blank = [-1,-1,-1,-1,-1,-1,-1,-1,-1]

        copyin = orig.copy()
        output = blank.copy()
        ninesym.sym_horizontal.transform(copyin, output)
        assert orig == [0,1,2,3,4,5,6,7,8]
        assert copyin == [0,1,2,3,4,5,6,7,8]
        assert output == [2,1,0,  5,4,3,  8,7,6]

        output = blank.copy()
        ninesym.sym_vertical.transform(copyin, output)
        assert output == [6,7,8, 3,4,5, 0,1,2]

        output = blank.copy()
        ninesym.sym_lr_diag.transform(copyin, output)
        assert output == [8,5,2, 7,4,1, 6,3,0]

        output = blank.copy()
        ninesym.sym_rl_diag.transform(copyin, output)
        assert output == [0,3,6, 1,4,7, 2,5,8]

        **/
}
