package com.tiemens.tictactoe.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.tiemens.tictactoe.math.NineSymmetry.IndexFlip;
import com.tiemens.tictactoe.math.NineSymmetry.Transformer;

class NineSymmetryTest {

    public void log(String s) {
        System.out.println(s);
    }
    
    @Test
    void testSymmetry() {
        //NineSymmetry ninesym = new NineSymmetry();
        List<Integer> npnine = List.of(0,1,2,3,4,5,6,7,8);
        log("Orig          = " + NineSymmetry.computeOutputString(npnine));
        String expected;
        String computed;
        
        List<Integer> flipupd = List.of(6,7,8,  3,4,5,  0,1,2); 
        expected = NineSymmetry.computeOutputString(flipupd);
        computed = NineSymmetry.computeOutputStringForTransformer(npnine, IndexFlip.sym_vertical);
        assertEquals(expected, computed, "FlipUD");
        log("hand.FlipUpd  = " + expected);
        log("NS.sym_vert   = " + computed);

        List<Integer> fliplr = List.of(2,1,0,   5,4,3,  8,7,6);
        expected = NineSymmetry.computeOutputString(fliplr);
        computed = NineSymmetry.computeOutputStringForTransformer(npnine, IndexFlip.sym_horizontal);
        assertEquals(expected, computed, "FlipLR");
        log("np.FlipLr     = " + expected);
        log("NS.sym_vert   = " + computed);        
    }
    
    @Test
    void testComputeAllUnique() {
        NineSymmetry ninesym = new NineSymmetry();
        Map<String, Transformer> output2transform = ninesym.computeAllUnique();
        // # 1 "identity" transform plus 7 other unique transforms:
        assertEquals(8,  output2transform.keySet().size());
    }
    
    @Test
    void testStringSymmetries() {
        // get the transformers
        NineSymmetry ninesym = new NineSymmetry();
        Collection<Transformer> transformers = ninesym.computeAllUniqueNotIdentity().values();

        String input;
        List<String> expected;
        
        input = "012345678";
        expected = List.of("036147258", "210543876", "258147036", "630741852", "678345012", "852741630", "876543210");
        subtestStrings(ninesym, transformers, input, expected);
        
        // symmetry is fun
        expected = new ArrayList<>(expected); // make it mutable
        expected.add(1, input);      // the previous input becomes (almost) 1st element
        input = expected.remove(0);  // the previous 1st element becomes input
        subtestStrings(ninesym, transformers, input, expected);
    }

    private void subtestStrings(NineSymmetry ninesym, Collection<Transformer> transformers, String input, List<String> expected) {
        List<String> actual = ninesym.generateSymmetricKeys(transformers, input);
        System.out.println(" IN " + input);
        for (String s : actual) {
            System.out.println("   OUT " + s);
        }
        Assert.assertEquals(expected.size(), actual.size());
        Assert.assertEquals(expected, actual);
        
    }
    
    
    @Test
    void testOneBadBoardStateSymmetry() {
        // get the transformers
        NineSymmetry ninesym = new NineSymmetry();
        Collection<Transformer> transformers = ninesym.computeAllUniqueNotIdentity().values();

        String input;
        List<String> expected;

        // our first "troublesome" board state:
        input = "bbboboxxx";
        expected = List.of("boxbbxbox", "xobxbbxob", "xxxobobbb");
        subtestStrings(ninesym, transformers, input, expected);
    }
}
