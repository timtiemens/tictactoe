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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        writeUniqueToFilename(outfilepath, 
                              data.header, data.key_list, data.uniquestring2line);
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
                                       Map<String, String> uniquestring2line) {
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
            //log(f"key {key} to {line}")
            //List<Integer> ninearrays = new ArrayList<>();
            //original.forEach(  (String theline) -> ninearrays.add(cvtToNinearray(theline))  );
        }
         
        // logf" nine[0][0]={ninearrays[0][0]}")   # prints  "b"
        List<String> keep_lines = new ArrayList<>();
        Set<String> seen_keys = new HashSet<>();
        List<String> key_list = new ArrayList<>(uniquestring2line.keySet());

        // log("  sample key ({key_list[0]})")
        for (String key : key_list) {
            //log(" number of keys={len(uniquestring2line.keys())}")
            if (uniquestring2line.containsKey(key)) {
                if (! seen_keys.contains(key)) {
                    seen_keys.add(key);
                }
                // now remove all of the symmetry-identical keys
                List<String> symmetric_keys = generateSymmetricKeys(transforms, key);
                // log(" number symmetric_keys={len(symmetric_keys)}")
                for (String delete_key : symmetric_keys) {
                    //log(" checking delete_key ({delete_key})")
                    if (uniquestring2line.containsKey(delete_key)) {
                        String removed = uniquestring2line.remove(delete_key);
                        if (removed == null) {
                            throw new RuntimeException("programmer error delete_key=" + delete_key);
                        } else {
                            //log("success removing key " + delete_key);
                        }
                    }
                }
            }
        }

        //return uniquestring2line;
        return new ProcessedData(header, key_list, uniquestring2line); 
    }


    public void writeUniqueToFilename(String outfilename, String header, List<String> key_list, Map<String, String> uniquestring2line) {
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

    
    
    public String cvtLineToKey(String line) {
        List<String> ret = new ArrayList<>(Arrays.asList(line.split(",")));
        ret.remove( ret.size() - 1);
        return String.join(",", ret);
    }

    public String cvtNinearrayToKey(List<String> ninearray) {
        return String.join(",", ninearray); // "clean", no "[ ]"
    }

    /**
     * 
     * @param key is "0","1",...,"8"   i.e. comma-separated, but still a simple string
     * @return
     */
    public List<String> cvtToNinearray(String key) {
        String[] pieces = key.split(",");
        List<String> ret = new ArrayList<>();
        for (String item : pieces) {
            ret.add(item);
        }
        return ret;
    }
    
    public List<String> generateSymmetricKeys(Collection<Transformer> transformers, String key) {
        List<String> ret = new ArrayList<>();
        List<String> ninearray = cvtToNinearray(key);
        
        for (Transformer transformer : transformers) {
            List<String> output = new ArrayList<>(ninearray);
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
    }

}
