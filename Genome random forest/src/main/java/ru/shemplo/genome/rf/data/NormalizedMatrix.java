package ru.shemplo.genome.rf.data;

import static ru.shemplo.snowball.utils.fun.StreamUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.shemplo.snowball.stuctures.Pair;

@AllArgsConstructor @Getter
public class NormalizedMatrix {

                                // rows    columns
    private final List <String> genesName, entitiesName;
    private final double [][] matrix;
    
    private final SourceDataset dataset;
    
    /**
     * Subtracts matrix with specified bounds 
     * from current instance
     * 
     * <pre>
     *     hf    ht 
     * +---+-----+--------------+
     * |   .     .              |
     * |   .     .              |
     * |   .     .              |
     * |   ++++++- - - - - - - -+ vt
     * |   ++++++-              |
     * |   ++++++-              |
     * |   ++++++-              |
     * |   ------- - - - - - - -+ vt
     * |                        |
     * |                        |
     * |                        |
     * |                        |
     * |                        |
     * +------------------------+
     * </pre>
     * 
     * @param vf vertical from   (inclusive)
     * @param vt vertical to     (exclusive)
     * @param hf horizontal from (inclusive)
     * @param ht horizontal to   (exclusive)
     * 
     * @return
     * 
     */
    public NormalizedMatrix getSubMatrix (int vf, int vt, int hf, int ht) {
        checkHorizontal (hf, "hf"); checkHorizontal (ht - 1, "ht");
        checkVertical (vf, "vf"); checkVertical (vt - 1, "vt");
        
        double [][] sub = new double [vt - vf][ht - hf];
        for (int i = 0; i < sub.length; i++) {
            System.arraycopy (matrix [vf + i], hf, sub [i], 0, ht - hf);
        }
        List <String> subGenes    = genesName.subList    (vf, vt), 
                      subEntities = entitiesName.subList (hf, ht);
        
        return new NormalizedMatrix (subGenes, subEntities, sub, dataset);
    }
    
    private void checkHorizontal (int req, String index) {
        if (req < 0 || req >= matrix [0].length) {
            throw new IndexOutOfBoundsException (index);
        }
    }
    
    private void checkVertical (int req, String index) {
        if (req < 0 || req >= matrix.length) {
            throw new IndexOutOfBoundsException (index);
        }
    }
    
    /**
     * Shuffles order of rows of current instance 
     * and returns as new {@link NormalizedMatrix}
     * 
     * @return
     * 
     */
    public NormalizedMatrix getShuffledMatrix () {
        List <Pair <String, Integer>> 
            pairs = zip (genesName.stream (), Stream.iterate (0, i -> i + 1), 
                         Pair::mp)
                  . collect (Collectors.toList ());
        Collections.shuffle (pairs, new Random ());
        List <String> genes = pairs.stream ().map (p -> p.F).collect (Collectors.toList ()), 
                      entities = new ArrayList <> (entitiesName);
        
        int width = matrix [0].length;
        double [][] matrix = new double [this.matrix.length][width];
        for (int i = 0; i < this.matrix.length; i++) {
            int from = pairs.get (i).S;
            System.arraycopy (this.matrix [from], 0, 
                  matrix [i], 0, matrix [i].length);
        }
        
        return new NormalizedMatrix (genes, entities, matrix, dataset);
    }
    
    public double denormalizeValue (String gene, double value) {
        double max = -1.0, min = Double.MAX_VALUE;
        for (int i = 0; i < entitiesName.size (); i++) {
            String name = entitiesName.get (i);
            SourceEntity entity = 
                    dataset.getEntityByGeoAccess (name);
            double exp = entity.getExpressionByGene (gene);
            max = Math.max (max, exp);
            min = Math.min (min, exp);
        }
        
        return value * (max - min) + min;
    }
    
    public int getNumberOfEntities () {
        return entitiesName.size ();
    }
    
    public int getNumberOfGenes () {
        return genesName.size ();
    }
    
    public int getRowOfGene (String gene) {
        for (int i = 0; i < genesName.size (); i++) {
            if (genesName.get (i).contains (gene)) { return i; }
        }
        
        return -1;
    }
    
}
