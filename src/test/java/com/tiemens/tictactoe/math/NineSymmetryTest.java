package com.tiemens.tictactoe.math;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

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
}
