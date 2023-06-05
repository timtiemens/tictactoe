package com.tiemens.tictactoe.generate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.tiemens.tictactoe.math.NineSymmetry;
import com.tiemens.tictactoe.math.NineSymmetry.Transformer;

public class RemoveDuplicates {
    public static String inputfile = "ttt-endgame.csv";
    public static String outputfile = "ttt-endgame-unique.csv";
    
    public static String inputdirectory = "src/data";
    public static String outputdirectory = "build/data";

    public void removeAllDuplicates(String indir, String infilename, 
                                    String outdir, String outfilename) {
        String infilepath = indir + "/" + infilename;
        List<String> allLines = readAllLines(infilepath);
        
        ProcessedData data = processLinesForUniqueUnderSymmetry(allLines);

        String outfilepath = outdir + "/" + outfilename;
        new File(outdir).mkdirs();
        writeUniqueToFilename(outfilepath, data);
        
        writeDetailUniqueToDirectory(outdir + "/" + "detailsremovedupes",
                                     outdir + "/" + "detailsremovedupesNines",
                                     data);

    }
    

    public List<String> readAllLines(String infilename) {
        Path filePath = Paths.get(infilename);
        Charset charset = StandardCharsets.UTF_8;
        try {
            List<String> lines = Files.readAllLines(filePath, charset);
            System.out.println("Number of lines: " + lines.size());
            //for(String line: lines) {
            //    System.out.println(line);
            //}
            return lines;            
        } catch (IOException ex) {
            System.err.format("I/O error: %s%n", ex);
            throw new RuntimeException(ex);
        }
    }


    public static record ProcessedData(String header,
                                       List<String> key_list,
                                       Map<String, String> uniquestring2line,
                                       Map<String, List<String>> seen_keys_to_replaced_keys,
                                       Map<String, String> uniquestring2originalline) 
    {
    };
    

    /**
     * Read infilename.csv, ignore the header, read all lines into LIST.
     *  while len(LIST) > 0:
     *   line = remove 1st element
     *   add line to KEEP
     *    now compute all rotations, flips, etc
     *   for each equalnine = create_all_symmetries(line): 
     *     if equalnine is in LIST:
     *      remove equalnin from LIST
     * Write outfilename.csv
     * 
     * @param lines 
     * @return uniquelines
     */
    public ProcessedData processLinesForUniqueUnderSymmetry(List<String> original) {
        String header = original.remove(0);
        
        NineSymmetry ninesym = new NineSymmetry();
        Collection<Transformer> transforms = ninesym.computeAllUniqueNotIdentity().values();
                
        Map<String, String> uniquestring2line = new HashMap<>();
        for (String line :  original) {
            String key = cvtLineToKey(line);
            uniquestring2line.put(key, line);
        }
        // keep the key->originalline mapping
        Map<String, String> uniquestring2originalline = new HashMap<>(uniquestring2line);
        
        System.out.println("Uniquestring2line.keys().size=" + uniquestring2line.keySet().size());
         
        List<String> keep_lines = new ArrayList<>();
        Map<String, List<String>> seen_keys_to_replaced_keys = new HashMap<>();
        List<String> key_list = new ArrayList<>(uniquestring2line.keySet());

        // log("  sample key ({key_list[0]})")
        for (String key : key_list) {
            log(" LOOP key='" + key + "' number of keys=" + uniquestring2line.keySet().size());
            // this loop removes items from uniquestring2line, so "key" might not be in the list now:
            if (uniquestring2line.containsKey(key)) {
                if (! seen_keys_to_replaced_keys.containsKey(key)) {
                    seen_keys_to_replaced_keys.put(key, new ArrayList<>());
                    log("    LOOP added new arraylist for key='" + key + "'");
                }
                // now remove all of the symmetry-identical keys
                List<String> symmetric_keys = generateSymmetricKeys(transforms, key);
                log("    LOOP number of symmetric_keys=" + symmetric_keys.size());
                // log(" number symmetric_keys={len(symmetric_keys)}")
                int count_removed = 0;
                for (String delete_key : symmetric_keys) {
                    //log(" checking delete_key ({delete_key})")
                    if (uniquestring2line.containsKey(delete_key)) {
                        String removed = uniquestring2line.remove(delete_key);
                        count_removed++;
                        log("    LOOP removed key=" + delete_key + " count=" + count_removed);
                        if (removed == null) {
                            throw new RuntimeException("programmer error delete_key=" + delete_key);
                        } else {
                            //log("success removing key " + delete_key);
                            seen_keys_to_replaced_keys.get(key).add(delete_key);
                        }
                    } else {
                        log("    LOOP did not find symmetric key=" + delete_key);
                    }
                }
            } else {
                System.out.println("  previously removed key " + key);
            }
        }

        //return uniquestring2line;
        return new ProcessedData(header, 
                                 key_list, 
                                 uniquestring2line, 
                                 seen_keys_to_replaced_keys,
                                 uniquestring2originalline); 
    }

    private void writeDetailUniqueToDirectory(String outdirectory, String outNinesDirectory, ProcessedData data) {
        String header = data.header;
        List<String> key_list = data.key_list;
        Map<String, String> uniquestring2line = data.uniquestring2line;
        Map<String, List<String>> seen_keys_to_replaced_keys = data.seen_keys_to_replaced_keys;
        Map<String, String> uniquestring2originalline = data.uniquestring2originalline;         
        
        final File outdirFile = new File(outdirectory);
        outdirFile.mkdirs();
        int countDelete = 0;
        for (File file : outdirFile.listFiles()) {
            if (! file.isDirectory()) {
                file.delete();
                countDelete++;
            }
        }
        System.out.println("OutDirectory " + outdirectory + " deleted " + countDelete + " files");
        
        final File outdirNinesFile = new File(outNinesDirectory);
        outdirNinesFile.mkdirs();
        countDelete = 0;
        for (File file : outdirNinesFile.listFiles()) {
            if (! file.isDirectory()) {
                file.delete();
                countDelete++;
            }
        }
        System.out.println("OutDirectory " + outdirectory + " deleted " + countDelete + " files");
        
        System.out.println("Begin processing keys->replacedkeys size=" + seen_keys_to_replaced_keys.keySet().size());
        //for (String key : seen_keys_to_replaced_keys.keySet()) {
        for (String key : key_list) {
            if (seen_keys_to_replaced_keys.containsKey(key))  {            
                System.out.println("*** " + key + " " + seen_keys_to_replaced_keys.get(key).size());
                
                writeDetailsToFilename(outdirFile, 
                                       key, 
                                       seen_keys_to_replaced_keys.get(key),
                                       uniquestring2originalline,
                                       identity);
                
                writeDetailsToFilename(outdirNinesFile,
                                       key, 
                                       seen_keys_to_replaced_keys.get(key), 
                                       uniquestring2originalline,
                                       changetoNinechars);
                
                Consumer<String> c;
            } else {
                System.out.println(" detail write skipping key " + key);
            }
        }        
        
    }
    
    private UnaryOperator<String> identity = (String text) -> { return text;} ;
        
    
    private UnaryOperator<String> changetoNinechars = (String s) -> {
        String ret = s;
        s = s.replace("positive", "");
        s = s.replace("negative", "");
        s = s.replace("\"", "");
        s = s.replace(",", "");
        return s;
    };

    // create a file named "key" with the contents being the replaced keys converted to their original lines
    private void writeDetailsToFilename(File outdirFile, 
                                        String key,  
                                        List<String> replaced_keys,
                                        Map<String, String> uniquestring2originalline,
                                        UnaryOperator<String> operator) {
        String firstLine = null;
        String outfilename = new File(outdirFile, key).toString();
     
        // Build all of the output lines:
        List<String> outlines = new ArrayList<>();
        if ((replaced_keys == null) || (replaced_keys.size() == 0) || (! replaced_keys.contains(key))) {
            firstLine = operator.apply(uniquestring2originalline.get(key)) + "\n";
            outlines.add(firstLine);
        }
        for (String replacedKey : replaced_keys) {
            String originalline = uniquestring2originalline.get(replacedKey);
            if (originalline != null) {
                outlines.add(operator.apply(uniquestring2originalline.get(replacedKey)) + "\n");
            } else { 
                throw new RuntimeException("failed original for replacedKey='" + replacedKey + "'");
            }
        }
        
        // sort:
        Collections.sort(outlines);
        
        if (! outlines.get(0).equals(firstLine)) {
            
        } else {
            System.out.println(" ----   firstline is the same after sort");
        }
        
        // Write all of the (sorted) output lines:
        try (FileWriter writer = new FileWriter(outfilename)) {
            for (String line : outlines) {
                writer.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public void writeUniqueToFilename(String outfilename, ProcessedData data) {
        String header = data.header;
        List<String> key_list = data.key_list;
        Map<String, String> uniquestring2line = data.uniquestring2line;
        Map<String, List<String>> seen_keys_to_replaced_keys = data.seen_keys_to_replaced_keys;


    
        try (FileWriter writer = new FileWriter(outfilename)) {
            writer.write(header + "\n");
            for (String key : key_list) {
                if (uniquestring2line.containsKey(key)) {
                    writer.write(uniquestring2line.get(key) + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    
    /**
     * 
     * @param line from ttt-endgame.csv, e.g. "b","b","b","b","o","o","x","x","x","positive"
     * @return bbbbooxxx
     */
    public String cvtLineToKey(String line) {
        String quote = "\"";
        List<String> split = new ArrayList<>(Arrays.asList(line.split(",")));
        split.remove( split.size() - 1);
        
        String ret = String.join("", split).replace(quote, "");
        //log(" cvtLineToKey ret=" + ret);
        return ret;
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    public String cvtNinearrayToKey(List<String> ninearray) {
        return String.join("", ninearray); // "clean", no "[ ]"
    }

    /**
     * 
     * @param key is "012345678"   i.e. simple string, length 9
     * @return List<"0","1",...,"8">  size 9   i.e. comma-separated, but still a simple string
     */
    public List<String> cvtToNinearray(String key) {
        List<String> ret = new ArrayList<>();
        for (int i = 0, n = key.length(); i < n; i++) {
            ret.add("" + key.charAt(i));
        }
        if (ret.size() != 9) {
            throw new RuntimeException("programmer error on length, key='" + key + "' ret=" + ret);
        }
        return ret;
    }
    
    public List<String> generateSymmetricKeys(Collection<Transformer> transformers, String key) {
        List<String> ret = new ArrayList<>();
        List<String> ninearray = cvtToNinearray(key);
        
        for (Transformer transformer : transformers) {
            List<String> output = new ArrayList<>(ninearray);
            //log("applying transformer " + transformer.getName() + " to ninearray " + ninearray);
            transformer.transform(ninearray, output);
            if (false) {
                //log(" transform named {transformer.get_name()} {ninearray}");
                //log(f"                     {output}");
            }
            String outputstring = cvtNinearrayToKey(output);
            ret.add(outputstring);
        }
            
        return ret;
    }
    
    public static void main(String[] args) {
        new RemoveDuplicates().removeAllDuplicates(inputdirectory, inputfile, 
                                                   outputdirectory, outputfile);
        System.out.println("Main finished");
    }

}
