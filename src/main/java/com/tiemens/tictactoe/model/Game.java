package com.tiemens.tictactoe.model;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.tiemens.tictactoe.model.Board.RowCol;

public class Game {

    public static enum Strategy {
        Random() {
            public RowCol move(Board board, CellValue usePlayer, CellValue otherPlayer) {
                //  does not use either player to decide:
                List<RowCol> items = board.listEmpty();
                int index = random.nextInt(items.size());
                return items.get(index);
            }
        },
        First() {
            public RowCol move(Board board, CellValue usePlayer, CellValue otherPlayer) {
                // does not use either player to decide:
                List<RowCol> items = board.listEmpty();
                return items.get(0);
            }
        }, 
        Best() {
            public RowCol move(Board board, CellValue usePlayer, CellValue otherPlayer) {
                return board.strategyBest(usePlayer, otherPlayer);
            }
        }, 
        Perfect() {
            public RowCol move(Board board, CellValue usePlayer, CellValue otherPlayer) {
                return board.strategyPerfect(usePlayer, otherPlayer);
            }
        };
        
        public abstract RowCol move(Board board, CellValue usePlayer, CellValue otherPlayer);
    } // strategy
    
    private CellValue theFirstPlayer;
    private CellValue theSecondPlayer;
    private CellValue currentPlayer;
    private Board board;
    private Map<CellValue, Strategy> player2Strategy = null;
    private Object gameWinner = null;
    private static Random random = new Random(1);
    
    public Game(CellValue theFirstPlayer, CellValue theSecondPlayer) {
        this.theFirstPlayer = theFirstPlayer;
        this.theSecondPlayer = theSecondPlayer;
        this.currentPlayer = theFirstPlayer;
        this.gameWinner = null;
        this.board = new Board();
        
        this.player2Strategy = new HashMap<CellValue, Strategy>();
        this.player2Strategy.put(theFirstPlayer, Strategy.Random);
        this.player2Strategy.put(theSecondPlayer, Strategy.Best);
    }

    public Game(Game otherGame) {
        this(otherGame.theFirstPlayer, otherGame.theSecondPlayer);
        this.board = new Board(otherGame.board);
        this.currentPlayer = otherGame.currentPlayer;
        this.player2Strategy = new HashMap<>(otherGame.player2Strategy);
    }            
            
    // TODO: change to factory?
    public Game(List<Integer> indexArray, CellValue theFirstPlayer, CellValue theSecondPlayer) {       
        this(theFirstPlayer, theSecondPlayer);
        for (Integer index : indexArray) {
            if (this.getGameWinner() == null) {
                //r = int( index / 3 )
                //c = index - (r * 3)
                this.playerMakesMove(index, this.currentPlayer);  
                // currentPlayer has been updated
            } else {
                throw new RuntimeException("Create game index={index} game winner already {game.get_game_winner()}");
            }
         }
     }
    
    public static void setRandomSeed(long seed) {
        Game.random.setSeed(seed);
    }


     public Board getBoard() {
         return this.board;
     }
     
     public Strategy getStrategyForPlayer(CellValue player) {
         Strategy ret = this.player2Strategy.get(player);
         // log(" getstrategy(" + player + ") ret=" + ret);
         return ret;
     }

     public void playerMakesMove(int index, CellValue thePlayer) {
         this.board.setCellRowCol(RowCol.lookupIndex(index), thePlayer);
         this.checkGame();
         if (this.getGameWinner() == null) {
             this.currentPlayer = nextPlayer(thePlayer);
         } else {
             //System.out.println("At end of turn, getGameWinner is " + this.getGameWinner());
         }
     }

     public CellValue nextPlayer(CellValue fromPlayer) {
         if (CellValue.X.equals(fromPlayer)) {
             return CellValue.O;
         } else {
             return CellValue.X;
         }
     }
     
     public void newGame() {
         this.board.clearAllCells();
         this.gameWinner = null;
         this.currentPlayer = this.theFirstPlayer;
     }
     
     public void checkGame() {
         CellValue win = this.board.getWinner();
         int count = this.board.countEmpty();
         boolean anyEmpty = (count > 0);

         if ((win == null) && (! anyEmpty)) {
             this.gameWinner = "NONE"; 
         } else {
             if (win != null) {
                 this.gameWinner = win;
             }
         }
     }

     public void doATurn() {
         this.doATurnFollowingStrategy(this.getStrategyForPlayer(this.currentPlayer));
     }
     
     public void doATurnFollowingStrategy(Strategy strategy) {
         //#print(f"datfs strategy={strategy}")
         //   print(f"do a turn, current={self.currentPlayer}")
         CellValue use_player = this.currentPlayer;
         CellValue other_player = this.nextPlayer(use_player);
         RowCol move = strategy.move(this.board, use_player, other_player);
         //log("player " + use_player + " strategy=" + strategy + " decided on " + move);
         this.playerMakesMove(move.getIndex(), use_player);
     }
      
   

     public RowCol movePerfectEmpty(Board board, CellValue usePlayer, CellValue otherPlayer) {     
         return board.strategyPerfect(usePlayer, otherPlayer);
     }

     // board point-of-view winner
     public CellValue getBoardWinner() {
         // NOTE: board.getWinner() does not detect "DRAW" - it just returns None
         return this.board.getWinner();
     }
     
     // actual game winner
     public Object getGameWinner() {
         // return CellValue or String:
         return this.gameWinner;
     }
        
     public void printState() {
         printState(System.out);
     }
     
     public void printState(PrintStream out) {
         //out.println("first player = " + theFirstPlayer + " strategy=" + player2Strategy.get(theFirstPlayer));
         //out.println("secon player = " + theSecondPlayer+ " strategy=" + player2Strategy.get(theSecondPlayer));
         //out.println("p2strat      = " + player2Strategy);
         
         System.out.println(this.board.toStringState());
     }


}
