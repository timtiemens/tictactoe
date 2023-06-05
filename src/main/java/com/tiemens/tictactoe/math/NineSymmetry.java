package com.tiemens.tictactoe.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class NineSymmetry {

    public static abstract class Transformer {
        private final String name;
        public Transformer(String name) {
            this.name = name;
        }
        public abstract <T> void transform(List<T> ninein, List<T> nineout);
        public final String getName() { 
            return name;
        }
    } // Transformer

    public static class TransformerChain extends Transformer {
        private List<Transformer> transformerList = new ArrayList<>();
        public TransformerChain(Transformer... chain) {
            super(getcomboname(chain));
            for (Transformer transformer : chain) {
                transformerList.add(transformer);
            }
        }
   
        private static String getcomboname(Transformer[] chain) {
            List<String> names = new ArrayList<>();
            for (Transformer transformer : chain) {
                names.add(transformer.getName());
            }
            return String.join("_", names);
        }

        public <T> void transform(List<T> ninein, List<T> nineout) {
            if (ninein == nineout) {
                throw new RuntimeException("ERROR transformchain:  in == out will not end well.");
            }

            List<T> input = new ArrayList<>(ninein); //   # just for extra safety
            List<T> output = nineout;
            // # note: cannot make a copy of nineout, since that is the actual output
            for (Transformer transformer : transformerList) {
                // #print(f" Running transform {transform.get_name()} {transform}")
                transformer.transform(input, output);
                // bug potential   "input = output;" leads to bad things
                input = new ArrayList<>(output);
            }
        }
    } // TransformerChain
    
    public static class IndexFlip extends Transformer {
        private static List<Integer> standardNineIn = List.of(0,1,2,3,4,5,6,7,8);
        public static List<Transformer> allSimple;
        public static List<Transformer> allComplete;
        private List<Integer> index_in;
        private List<Integer> index_out;
        public IndexFlip(String namein, List<Integer> nineout) {
            this(namein, nineout, standardNineIn);
        }
        public IndexFlip(String namein, List<Integer> nineout, List<Integer> ninein) {
            super(namein);
            this.index_in = ninein;
            this.index_out = nineout;
            if (this.index_in.size() != 9) {
                throw new RuntimeException(String.format("In %s Length must be 9, instead was %d", namein, index_in.size()));
            }
            if (this.index_out.size() != 9) {
                throw new RuntimeException(String.format("Out %s Length must be 9, instead was %d", namein, index_out.size()));
            }
            for (int i = 0, n = 9; i < n; i++) {
                if (! this.index_in.contains(i)) {
                    throw new RuntimeException(String.format("In %s Symmtetry missing index %d", namein, i));
                }
                if (! this.index_out.contains(i)) {
                    throw new RuntimeException(String.format("Out %s Symmetry missing index %d", namein, i));
                }
            }
        }

        public String toString() {
            return listintegerToString(index_out, " ");
            //return "[" + String.join(" ",  index_out.stream().map(String::valueOf).collect(Collectors.toList())) + "]";
        }

        public <T> void transform(List<T> ninein, List<T> nineout) {
            if (ninein == nineout) {
                throw new RuntimeException("ERROR indexflip: in == out will not end well.");
            }
            for (int i = 0, n = index_in.size(); i < n; i++) {
                int source = index_in.get(i);
                int target = index_out.get(i);
                nineout.set(target,  ninein.get(source));
            }
        }
              
        static {
            createAll();
        }
        public static Transformer sym_horizontal;
        public static Transformer sym_vertical;
        public static void createAll() {
            IndexFlip.sym_horizontal = new IndexFlip("hor", List.of(2,1,0, 5,4,3, 8,7,6));
            IndexFlip.sym_vertical   = new IndexFlip("ver", List.of(6,7,8, 3,4,5, 0,1,2));
            Transformer sym_lr_diag    = new IndexFlip("lrd", List.of(8,5,2, 7,4,1, 6,3,0));
            Transformer sym_rl_diag    = new IndexFlip("rld", List.of(0,3,6, 1,4,7, 2,5,8));
            //#self.sym_err    = NineSymmetry.IndexFlip("err", [0,3,6, 1,4,7, 2,5,8])   
            Transformer tc_hor_vert    = new TransformerChain(sym_horizontal,
                                                              sym_vertical);
            Transformer tc_vert_hor    = new TransformerChain(sym_vertical,
                                                              sym_horizontal);
            Transformer tc_hor_lrd     = new TransformerChain(sym_horizontal,
                                                              sym_lr_diag);
            Transformer tc_vert_lrd    = new TransformerChain(sym_vertical,
                                                              sym_lr_diag);
            List<Transformer> all_simple = List.of(
                    sym_horizontal,
                    sym_vertical,
                    sym_lr_diag,
                    sym_rl_diag);
            List<Transformer> all_complete = List.of(
                    sym_horizontal,
                    sym_vertical,
                    sym_lr_diag,
                    sym_rl_diag,
                    tc_hor_vert,
                    tc_vert_hor,
                    tc_hor_lrd,
                    tc_vert_lrd);
            
            IndexFlip.allSimple = all_simple;
            IndexFlip.allComplete = all_complete;
        }
    
    } // IndexFlip
    
    public static String listintegerToString(List<Integer> numbers) {
        return listintegerToString(numbers, " ");
    }
    public static String listintegerToString(List<Integer> numbers, String sep) {
        return "[" + listintegerToStringClean(numbers, sep) + "]";            
    }
    public static String listintegerToStringClean(List<Integer> numbers, String sep) {
        return String.join(sep,  numbers.stream().map(String::valueOf).collect(Collectors.toList()));
    }
    
    public Map<String, Transformer> computeAllUnique() {
        List<Integer> input = List.of(0,1,2,3,4,5,6,7,8);

        Map<String, Transformer> retOutput2Transformer = new HashMap<>();

        // add the "simple"
        for (Transformer transformer : IndexFlip.allSimple) {
            String outputstring = computeOutputStringForTransformer(input, transformer);
            if (! retOutput2Transformer.containsKey(outputstring)) {
                retOutput2Transformer.put(outputstring,  transformer);
            }
        }

        // add all pairings of "complex/complete"
        for (Transformer first : IndexFlip.allComplete) {
            for (Transformer second : IndexFlip.allComplete) {
                Transformer transformer = new TransformerChain(first, second);
                String outputstring = computeOutputStringForTransformer(input, transformer);
                if (! retOutput2Transformer.containsKey(outputstring)) {
                    // log(f"UNIQUE: Adding compound {first.get_name()} {second.get_name()} {outputstring}")
                    retOutput2Transformer.put(outputstring, transformer);
                } else {
                    // log(f"UNIQUE: rejected duplicate {outputstring}")
                }
            }
        }

        return retOutput2Transformer;
    }

    public Map<String, Transformer> computeAllUniqueNotIdentity() {
        Map<String, Transformer> ret = computeAllUnique();
        String identity = computeOutputString(List.of(0,1,2,3,4,5,6,7,8));
        if (ret.containsKey(identity)) {
            ret.remove(identity);
        } else {
            throw new RuntimeException("programmer error identity=" + identity);
        }
        return ret;
    }


    public static String computeOutputStringForTransformer(List<Integer> input, Transformer transformer) {
        List<Integer> output = new ArrayList<>(input);
        transformer.transform(input, output);
        return computeOutputString(output);
    }

    public static String computeOutputString(List<Integer> input) {
        String ret = listintegerToString(input, " ");

        return ret;
    }
    
    /**
     * Helper method to turn String -> List<String> -> transform -> String
     * @param transforms list to apply
     * @param input to apply each transformer -- note: must be length 9 characters
     * @return List<String>, sorted, as the output of each transform in transforms
     *           does NOT include the identity transform of input
     */
    public List<String> generateSymmetricKeys(Collection<Transformer> transformers, String input) {
        // 
        // Hideous bug:  "does NOT include the identity transform" can mean
        //         1)   do not apply transforms like "Horiz(Horiz(m))" which are "identity transforms"
        //         2)   do not return results that are exactly equal to the input
        //
        // Other bug: "bbboboxxx" would generate [boxbbxbox, boxbbxbox, xobxbbxob, xobxbbxob, xxxobobbb, xxxobobbb]
        //              i.e. duplicates
        Set<String> setret = new HashSet<>();
        List<String> transformerIn = cvtStringToNinearray(input);        
        for (Transformer transformer : transformers) {

            List<String> transformerOut = new ArrayList<>(transformerIn);
            transformer.transform(transformerIn, transformerOut);
            
            setret.add(cvtNinearrayToString(transformerOut));
        }
        // bug/design "fix" :
        if (setret.contains(input)) {
            setret.remove(input);
        }

        List<String> ret = new ArrayList<>(setret);
        Collections.sort(ret);
        
        return ret;
    }

    public String cvtNinearrayToString(List<String> ninearray) {
        return String.join("", ninearray);
    }

    /**
     * 
     * @param key is "012345678"   i.e. simple string, length 9
     * @return List<"0","1",...,"8">  size 9   i.e. comma-separated, but still a simple string
     */
    public List<String> cvtStringToNinearray(String string) {
        List<String> ret = new ArrayList<>();
        for (int i = 0, n = string.length(); i < n; i++) {
            ret.add("" + string.charAt(i));
        }
        if (ret.size() != 9) {
            throw new RuntimeException("programmer error on length, key='" + string + "' ret=" + ret);
        }
        return ret;
    }
    
}

