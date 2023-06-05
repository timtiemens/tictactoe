package com.tiemens.tictactoe.generate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tiemens.tictactoe.math.NineSymmetry;
import com.tiemens.tictactoe.math.NineSymmetry.Transformer;

public class AnalyzeCover {

    /**
     * Analyze the problem of -- a version has 138 "unique" and another version has 104 "unique"
     *  
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        File dir = new File("src/analyze");
        File in104 = new File(dir, "size104.txt");
        File in138 = new File(dir, "size138.txt");
        
        List<String> lines104 = readlines(in104);
        List<String> lines138 = readlines(in138);
        
        System.out.println("Lines104=" + lines104.size() + " Lines138=" + lines138.size());
        
        generateJoinOutput(new File("java-join-output.txt"), lines138, lines104);
        
        checkInternalConsistent("104", lines104);
        checkInternalConsistent("138", lines138);
    }

    // internally consistent means every line is not part of another line's "symmetry set"
    private static void checkInternalConsistent(String where, List<String> listLines) {

        NineSymmetry ninesym = new NineSymmetry();
        Collection<Transformer> transformers = ninesym.computeAllUniqueNotIdentity().values();
        int lineNumber = 0;
        Set<String> seen = new HashSet<>();
        
        for (String ninekey : listLines) {
            if (! seen.contains(ninekey)) {
                seen.add(ninekey);
            } else {
                System.out.println("ERROR " + where + " line " + lineNumber + " already seen " + ninekey);
            }
            List<String> symmetry = ninesym.generateSymmetricKeys(transformers, ninekey);
            //System.out.println(where + " " + lineNumber + " (" + ninekey + ") symmetry list [" + String.join(",", symmetry));
            
            for (String sym : symmetry) {
                if (listLines.contains(sym)) {
                    System.out.println(where + " " + lineNumber + " (" + ninekey + ") has symmetry [" + sym + "] that is in the original list");
                    System.out.println("   all=" + String.join(",", symmetry));
                }
            }
            lineNumber++;
        }
        System.out.println("End checkInternal for " + where + " at lineNumber=" + lineNumber + " seen.size=" + seen.size());
    }

    // re-create the output from the "join" command line
    private static void generateJoinOutput(File outfile, List<String> linesLong, List<String> linesShort) {
        PrintStream out = System.out;
        
        if (outfile != null) {
            try {
                out = new PrintStream(new FileOutputStream(outfile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        int indexLong = 0;
        int indexShort = 0;
        final int nLong = linesLong.size();
        final int nShort = linesShort.size();
        
        Collections.sort(linesLong);
        Collections.sort(linesShort);
        
        while ((indexLong < nLong) && (indexShort < nShort)) {
            final String longString = linesLong.get(indexLong);
            final String shortString = linesShort.get(indexShort);
            if (longString.equals(shortString)) {
                out.println(longString + " " + shortString);
                indexLong++;
                indexShort++;
            } else if (longString.compareTo(shortString) < 0) {
                out.println(longString + " ");
                indexLong++;
            } else {
                out.println(" " + shortString);
                indexShort++;
            }
        }
        while (indexLong < nLong) {
            final String longString = linesLong.get(indexLong);
            out.println(longString + " ");
            indexLong++;            
        }
        while (indexShort < nShort) {
            final String shortString = linesShort.get(indexShort);
            out.println(" " + shortString);
            indexShort++;            
        }
    }

    private static List<String> readlines(File infile) {
        List<String> ret;
        
        try {
            ret = Files.readAllLines(infile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return ret;
    }

}
