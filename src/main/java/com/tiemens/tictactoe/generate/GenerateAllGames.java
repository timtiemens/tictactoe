package com.tiemens.tictactoe.generate;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.tiemens.tictactoe.model.Board;
import com.tiemens.tictactoe.model.Board.RowCol;
import com.tiemens.tictactoe.model.CellValue;
import com.tiemens.tictactoe.model.Game;

public class GenerateAllGames {
    public List<Game> createAllGames() {
        List<Game> rtn = new ArrayList<>();
    
        for (int index = 0, n = 9; index < n; index++) {
            List<Game> seeds = new ArrayList<>();
            List<Integer> list = new ArrayList<>();
            list.add(index);
            Game game = new Game(list, CellValue.X, CellValue.O);
            seeds.add(game);
            
            for (Game start_game : seeds) {
                List<Game> start_games = new ArrayList<>();
                List<Game> finished_games = new ArrayList<>();
                start_games.add(start_game);
                boolean keep_going = true;
                while (keep_going) {
                    keep_going = createAllGamesRecursive(start_games, finished_games);
                    rtn.addAll(finished_games);
                    log("Just added " + finished_games.size()  + "  start_games=" + start_games.size() + " rtn=" + rtn.size());
                    finished_games.clear();
                }
            }
        }

        return rtn;
    }

    
    
    
    
    private boolean createAllGamesRecursive(List<Game> start_games, List<Game> finished_games) {
        if (start_games.size() <= 0) {
            log("Len start_games is 0");
            return false;
        }

        Game game = start_games.remove(0);
            
        if (game.getGameWinner() == null) {
            List<RowCol> items = game.getBoard().listEmpty();
            for (RowCol move : items) {
                Game copy_game = new Game(game);
                copy_game.playerMakesMove(move.getIndex(), copy_game.getCurrentPlayer());
                Object winner = copy_game.getGameWinner();
                if (winner == null) {
                    start_games.add(0, copy_game);
                } else {
                    //  regardless of if the winner is "NONE" or X or O,
                    //   this is a "dead" game:
                    finished_games.add(copy_game);
                }
            }
        } else {
            log("Just pulled a non-None game from start_games");
            // TODO: decide if bug or not:
            throw new RuntimeException("start games should never contain a finished game");
            //finished_games.add(game);
        }

        return (start_games.size() != 0);

    }

    public void writeAllGames(List<Game> all_games, String outfilename) {
        try (FileWriter writer = new FileWriter(outfilename)) {
            // this is size 255,168 - the number of "play-chains" available in tic tac toe
            log("writeall games, size=" + all_games.size());
            printListGameStats(all_games);
            
            // this is size 958 - multiple play-chains result in the same board-state
            List<String> all_lines = cvtGamestoLines(all_games);
            log("writeall lines, size=" + all_lines.size());
            
            // this is size 104 - the same board-state has symmetrical equivalence
            // See RemoveDuplicates.java
            
            
            writeAllGames(all_lines, writer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void printListGameStats(List<Game> games) {
        final int size = games.size();
        int count_x = 0;
        int count_o = 0;
        int count_draw = 0;
        int count_unfinished = 0;
        for (Game game : games) {
            if (game.getGameWinner() == null) {
                count_unfinished++;
            } else {
                if (game.getGameWinner().equals(CellValue.X)) {
                    count_x++;
                } else if (game.getGameWinner().equals(CellValue.O)) {
                    count_o++;
                } else if (game.getGameWinner().equals("NONE")) {
                    count_draw++;
                } else {
                    throw new RuntimeException("Unknown gamewinner '" + game.getGameWinner() + "'");
                }
            }
        }
        PrintStream out = System.out;
        
        out.println("total number of games  " + size);
        out.println("total number x wins    " + count_x);
        out.println("total number o wins    " + count_o);
        out.println("total number draws     " + count_draw);
        out.println("total number unfinished " + count_unfinished);
    }





    public List<String> cvtGamestoLines(List<Game> all_games) {
        Set<String> outlines = new HashSet<>();
        String quote = "\"";
        
        for (Game game : all_games) {
            Board board = game.getBoard();
            List<Object> board_array = board.toBoardArray(CellValue.X);
            // list comprehension
            //List<String> board_array_quotes = board_array.stream().map(quote + Object::toString + quote).collect(Collectors.toList());
            // works List<String> board_array_quotes = board_array.stream().map(Object::toString).collect(Collectors.toList());            
            List<String> board_array_quotes = new ArrayList<>();
            board_array.forEach( item -> board_array_quotes.add(quote + item.toString() + quote));
            //List<String> board_array_quotes = board_array.stream().collect(Collectors.joining("''", quote, quote));
            
            //StringJoiner joiner = new StringJoiner(",", quote, quote);
            //joiner.add(board_array);
            //String line = joiner.toString();
            
            String line = String.join(",", board_array_quotes);
            
            line = line.replace("-", "b");
            line = line.replace("none", "negative");
            outlines.add(line);
        }

        List<String> listlines = new ArrayList<>();
        listlines.addAll(outlines);
        Collections.sort(listlines);
        return listlines;
    }
    
    public void writeAllGames(List<String> listlines, Writer writer) throws IOException {
        //
        // 0    1    2    3    4    5    6    7    8
        // "V1","V2","V3","V4","V5","V6","V7","V8","V9","V10"
        //  "x", "x", "x", "x", "o", "o", "x", "o", "o","positive"
        // The "simple just add" version creates a file with 255,169 lines.
        // This version filters out the duplicates and sorts
        //     After creating the ttt-endgame.csv file,
        // There are no differences between it and the sorted Kaggle .csv file.
        List<String> headerList = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            headerList.add("V" + i);
        }
        String newline = "\n";
        String quote = "\"";
        String sep = "";
        for (String col : headerList) {
            writer.write(sep + quote + col + quote);
            sep = ",";
        }
        writer.write("\n");
            

        for (String line : listlines) {
            writer.write(line + "\n");
        }

                
                        
    }



    private void log(String msg) {
        System.out.println(msg);
    }





    public static void main(String[] args) {
        GenerateAllGames generate = new GenerateAllGames();
        
        List<Game> games = generate.createAllGames();
        generate.writeAllGames(games, "build/all-games.csv");

    }

}
