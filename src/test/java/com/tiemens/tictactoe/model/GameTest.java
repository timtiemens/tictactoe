package com.tiemens.tictactoe.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameTest {

    @Test
    void testSingle() {
        System.out.println("Details on game:");
        sub_test(true, true);
    }
    
    @Test 
    void testTen() {
        for (int i = 0, n = 10;  i < n; i++) {
            sub_test(false, false);
        }
    }
    
    void sub_test(boolean resetRandom, boolean debug) {
        Game game = new Game(CellValue.X, CellValue.O);
        if (resetRandom) {
            Game.setRandomSeed(42);
        }
        while (game.getGameWinner() == null) {
            if (debug) { game.printState(); }
            game.doATurn();
        }
        System.out.println("Winner = " + game.getGameWinner());
        game.printState();
    }

}
