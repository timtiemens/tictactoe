package com.tiemens.tictactoe.model;

/**
 * Represents the state of a single cell on the board
 */
public enum CellValue {
    EMPTY("b") {
        public boolean isPlayer() {
            return false;
        }
    }, 
    X("x") {
        public boolean isPlayer() {
            return true;
        }
    },
    O("o") {
        public boolean isPlayer() {
            return true;
        }
    };
    public abstract boolean isPlayer();
    
    private String symbol;
    
    private CellValue(String symb) {
        this.symbol = symb;
    }
    
    public String toString() {
        return symbol;
    }

    public String getSymbol() {
        return symbol;
    }
   
}
