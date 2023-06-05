package com.tiemens.tictactoe.generate;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tiemens.tictactoe.math.NineSymmetry;
import com.tiemens.tictactoe.math.NineSymmetry.Transformer;
import com.tiemens.tictactoe.model.Board;
import com.tiemens.tictactoe.model.Board.KeyBoardState;
import com.tiemens.tictactoe.model.Board.RowCol;
import com.tiemens.tictactoe.model.CellValue;
import com.tiemens.tictactoe.model.Game;

public class GenerateAllGames {
    
    public static void main(String[] args) {
        GenerateAllGames generate = new GenerateAllGames();
        
        List<Game> games = generate.createAllGames();
        generate.writeAllFiles(games);

    }


    
    private void writeAllFiles(List<Game> games) {

        // The "games" list has 255,169 items in it.
        // The mapBoardState2Games has 958 keys (unique "board-states")

        // "games" is size 255,168 - the number of games (aka "play-chains") available in tic tac toe
        log("writeall games, size=" + games.size());
        printListGameStats(games);
        
        // this map has a keySet() of size 958:
        Map<KeyBoardState, List<Game>> mapBoardState2Games = cvtAllGamestoUniqueBoardStates(games);
        
        // After creating the ttt-endgame.csv file from those 958 keys,
        //   there are no differences between it and the original (sorted) Kaggle.csv file.
        writeAsKaggleCsv(mapBoardState2Games, "build/data/ttt-endgame.csv");
        writeAsNineChar(mapBoardState2Games,  "build/data/ttt-endgame.txt", false);
        writeAsNineChar(mapBoardState2Games,  "build/data/ttt-endgame-winlose.txt", true);        
        
        // TODO:  there are "rotational equivalents" in that list of 958.
        //        remove them, and the size is 104 - the same board-state has symmetrical equivalence
        //  See RemoveDuplicates.java
        Map<KeyBoardState, List<KeyBoardState>> retMapCoverer2ListCovereds = new HashMap<>();
        Map<KeyBoardState, List<Game>> mapWithNoRotationalDuplicates = 
                removeRotationalDuplicates(mapBoardState2Games, retMapCoverer2ListCovereds);
        writeAsKaggleCsv(mapWithNoRotationalDuplicates, "build/data/ttt-endgame-unique.csv");
        writeAsNineChar(mapWithNoRotationalDuplicates,  "build/data/ttt-endgame-unique.txt", false);
        writeAsNineChar(mapWithNoRotationalDuplicates,  "build/data/ttt-endgame-unique-winlose.txt", true);        
        
        System.out.println("Size NoRotationalDupes = " + mapWithNoRotationalDuplicates.keySet().size());
        System.out.println("Size Original          = " + mapBoardState2Games.keySet().size());
        
    }




    private void writeAsNineChar(Map<KeyBoardState, List<Game>> mapBoardState2Games, 
                                 String outfilename,
                                 boolean includeGameOutcome) {
        
        try (FileWriter writer = new FileWriter(outfilename)) {
            List<String> allLines = new ArrayList<>();
            for (KeyBoardState key : mapBoardState2Games.keySet()) {
                if (includeGameOutcome) {
                    String outcome = null;
                    for (Game game : mapBoardState2Games.get(key)) {
                        if (outcome == null) {
                            Object gameWinner = game.getGameWinner(); 
                            if (gameWinner == null) {
                                throw new RuntimeException("null gameWinner");
                            } else {
                                if (gameWinner.equals(CellValue.X)) {
                                    outcome = "win-x";
                                } else if (gameWinner.equals(CellValue.O)) {
                                    outcome = "win-o";
                                } else {
                                    outcome = "draw";
                                }
                            }
                        }
                    }
                    allLines.add(key.toString() + "," + outcome);
                } else {
                    allLines.add(key.toString());
                }
            }
            Collections.sort(allLines);      
            for (String s : allLines) {
                writer.write(s + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);            
        }
    }

    


    private void writeAsKaggleCsv(Map<KeyBoardState, List<Game>> mapBoardState2Games, String outfilename) {
        try (FileWriter writer = new FileWriter(outfilename)) {
                
            log("write kaggle csv to '" + outfilename + "', size=" + mapBoardState2Games.keySet().size());
            
            List<String> allLines = new ArrayList<>();
            for (KeyBoardState key : mapBoardState2Games.keySet()) {
                allLines.add(mapBoardState2Games.get(key).get(0).toKaggleCsvLine());
            }
            Collections.sort(allLines);            
            writeAsKaggleCsvWithHeaders(allLines, writer);           
     
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }



    /**
     * 
     * @return List of all possible (legal) Games (all 255k+ of them).
     */
    public List<Game> createAllGames() {
        List<Game> ret = new ArrayList<>();
    
        for (int index = 0, n = 9; index < n; index++) {
            List<Game> seeds = new ArrayList<>();

            Game game = Game.createFromTurnInde(List.of(index), CellValue.X, CellValue.O);
            seeds.add(game);
            
            for (Game start_game : seeds) {
                List<Game> start_games = new ArrayList<>();
                List<Game> finished_games = new ArrayList<>();
                start_games.add(start_game);
                boolean keep_going = true;
                while (keep_going) {
                    keep_going = createAllGamesRecursive(start_games, finished_games);
                    ret.addAll(finished_games);
                    //log("Just added " + finished_games.size()  + "  start_games=" + start_games.size() + " rtn=" + rtn.size());
                    finished_games.clear();
                }
            }
        }

        return ret;
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
            // This is a bug:
            throw new RuntimeException("start games should never contain a finished game");
        }

        return (start_games.size() != 0);

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
        
        
        // total number of games  255168
        // total number x wins    131184
        // total number o wins    77904
        // total number draws     46080
        // total number unfinished 0
        out.println("total number of games  " + size);
        out.println("total number x wins    " + count_x);
        out.println("total number o wins    " + count_o);
        out.println("total number draws     " + count_draw);
        out.println("total number unfinished " + count_unfinished);
    }




    @SuppressWarnings("unused")
    public Map<KeyBoardState, List<Game>> cvtAllGamestoUniqueBoardStates(List<Game> allGames) {
        log("Converting " + allGames.size() + " games KeyBoardState groupings...");

        // this records unique game-final-board-state strings, mapped to List<Game> that it "covers"
        Map<KeyBoardState, List<Game>> outmap = new HashMap<>();
        
        for (Game game : allGames) {
            Board board = game.getBoard();
            KeyBoardState keyBoardState = board.getKeyBoardState();  // aka String "xoxoxoxox"
            
            if (! outmap.containsKey(keyBoardState)) {
                outmap.put(keyBoardState, new ArrayList<>());
            }
            
            outmap.get(keyBoardState).add(game);
            
            
        }

        // TODO: better pattern for comparing?
        //Comparator.comparing(Game::getBoard()); // AgentSummaryDTO::getCustomerCount));

        // Order the List<Game> by the "Turn Index" of each Game
        for (KeyBoardState key : outmap.keySet()) {
            Collections.sort(outmap.get(key), 
                             (Game g1, Game g2) -> 
                g1.getBoard().getKeyTurnIndex().toString().compareTo(g2.getBoard().getKeyTurnIndex().toString()));
        }
        
        log("...that conversion resulted in " + outmap.keySet().size() + " lines.  " +
                "Typical key='" + outmap.keySet().stream().findFirst().get() + "'");

        
        // Deep-Dive: show one particular example how a Game-State "covers" multiple Games:
        if (false) {
            KeyBoardState testkey = new KeyBoardState("xoxoxoxox");
            log("   contains key='" + testkey + "' = " + outmap.containsKey(testkey));
            
            log("  the size of xoxoxoxox is " + outmap.get(testkey).size() + " which is 24*24, and 24=Permute(4)");

            // this just confirms they all print as "game=xoxoxoxox" ...
            //  for (Game game : outmap.get(key)) {
            //      log("   game=" + game.getBoard().toStringStateSingleLine());
            //}

            // this prints as "game turns=078563214"  i.e. the indexes of each turn, in order picked            
            for (Game game: outmap.get(testkey)) {
                log("  game turnskey=" + game.getBoard().getKeyTurnIndex().toString());
            }
        }
        
        return outmap;
    }


    
    public void writeAsKaggleCsvWithHeaders(List<String> listlines, Writer writer) throws IOException {
        //
        // 0    1    2    3    4    5    6    7    8
        // "V1","V2","V3","V4","V5","V6","V7","V8","V9","V10"
        //  "x", "x", "x", "x", "o", "o", "x", "o", "o","positive"
        
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
        writer.write(newline);

        for (String line : listlines) {
            writer.write(line + "\n");
        }
    }


    
    /**
     * 
     * @param mapBoardState2Games the original 958 board states
     * @param retMapCovered2Coverer output "helper" map
     * @return a new map with "rotational equivalents" removed
     */
    private Map<KeyBoardState, List<Game>> removeRotationalDuplicates(
            Map<KeyBoardState, List<Game>> mapBoardState2Games,
            Map<KeyBoardState, List<KeyBoardState>> retMapCoverer2ListCovereds) {
        
        Map<KeyBoardState, List<Game>> ret = new HashMap<>();
        retMapCoverer2ListCovereds.clear();
        
        // quick cache of "covered" board states
        Set<KeyBoardState> alreadyProcessed = new HashSet<>();
        
        // get the transformers
        NineSymmetry ninesym = new NineSymmetry();
        Collection<Transformer> transformers = ninesym.computeAllUniqueNotIdentity().values();

        // iterate the keys of mapBoardState2Games in sort order:
        List<KeyBoardState> activeWorkingKeys = new ArrayList<>(mapBoardState2Games.keySet());
        Collections.sort(activeWorkingKeys);
        
        for (KeyBoardState currentBoardState : activeWorkingKeys) {
            log(" LOOP key='" + currentBoardState + "' number of games=" + mapBoardState2Games.get(currentBoardState).size());
            // previous processing may have "covered" this keyBoardState -
            //   only proceed if it has not been seen:
            if (! alreadyProcessed.contains(currentBoardState)) {
                alreadyProcessed.add(currentBoardState);

                ret.put(currentBoardState, new ArrayList<>());
                ret.get(currentBoardState).addAll( mapBoardState2Games.get(currentBoardState) );

                //retMapCoverer2ListCovereds.put(activeBoardState, new ArrayList<>());
                // retMapCoverer2ListCovereds.put(keyBoardState, keyBoardState); // it covers itelf
                
                String keystring = currentBoardState.toString();
                List<String> symmetricKeys = ninesym.generateSymmetricKeys(transformers, keystring);            
                
                log("Coverer (" + currentBoardState + ") covers " + symmetricKeys.size() + " others");
                for (String coveredString : symmetricKeys) {
                    final KeyBoardState coveredKey = new KeyBoardState(coveredString);
                    
                    if (! alreadyProcessed.contains(coveredKey)) {
                        alreadyProcessed.add(coveredKey);
                        if (mapBoardState2Games.containsKey(coveredKey)) {
                            ret.get(currentBoardState).addAll( mapBoardState2Games.get(coveredKey) );
                        }
                        //retMapCovered2Coverer.put(coveredKey, keyBoardState);
                    }
                }

            } else {
                // this keyboardstate was covered, so add all of its Games to the one that covered it:
                //KeyBoardState targetKey = retMapCovered2Coverer.get(keyBoardState);
                //ret.get(targetKey).addAll(  mapBoardState2Games.get(keyBoardState) );
            }
        }

        System.out.println("After remove rotational duplicates, keyset.size=" + ret.keySet().size());
        return ret;
    }



    private void log(String msg) {
        System.out.println(msg);
    }


}
