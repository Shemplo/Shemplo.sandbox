package ru.shemplo.genome.rf.data;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class SourceDataset {

    private final Map <String, SourceEntity> entitiesMap = new HashMap <> ();
    private final List <SourceEntity> entitiesList = new ArrayList <> ();
    
    @Getter @Setter private String title;
    
    public void addEntity (SourceEntity entity) {
        if (entity == null) { return; }
        if (EntityVerdict.NEVUS.equals (entity.getVerdict ())) {
            return; // Classify only `normal` and `melanoma`
        }
        
        this.entitiesMap.put (entity.getGeoAccess (), entity);
        this.entitiesList.add (entity);
    }
    
    public void updateEntity (int index, Consumer <SourceEntity> consumer) {
        if (index >= 0 && index < entitiesList.size ()) {
            SourceEntity entity = getEntityByIndex (index);
            String backGeoAccess = entity.getGeoAccess ();
            consumer.accept (entity);
            
            if (!Objects.equals (backGeoAccess, entity.getGeoAccess ())) {
                if (backGeoAccess != null) entitiesMap.remove (backGeoAccess);
                entitiesMap.put (entity.getGeoAccess (), entity);
            }
        }
    }
    
    public SourceEntity getEntityByIndex (int index) {
        if (index >= 0 && index < entitiesList.size ()) {
            return entitiesList.get (index);
        }
        
        return null;
    }
    
    public SourceEntity getEntityByGeoAccess (String label) {
        return entitiesMap.get (label);
    }
    
    public int getSize () {
        return entitiesList.size ();
    }
    
    public NormalizedMatrix getNormalizedMatrix () {
        System.out.println ("[] Creating normalized matrix ...");
        
        Set <String> genes = new HashSet <> (entitiesList.get (0).getGenes ());
        entitiesList.stream ().map (e -> e.getGenes ()).forEach (genes::retainAll);
        List <String> entitiesName = entitiesMap.entrySet ().stream ()
                                   . filter (e -> e.getValue ().getVerdict () != EntityVerdict.NEVUS)
                                   . map (e -> e.getKey ()).collect (Collectors.toList ()), 
                      genesName = new ArrayList <> (genes);
        
        double [][] matrix = new double [genes.size ()][entitiesName.size ()];
        for (int i = 0; i < matrix.length; i++) {
            String geneName = genesName.get (i);
            for (int j = 0; j < matrix [i].length; j++) {
                String entityName = entitiesName.get (j);
                matrix [i][j] = getEntityByGeoAccess (entityName)
                              . getExpressionByGene (geneName);
            }
            
            normalizeRow (matrix [i]);
        }
        
        return new NormalizedMatrix (genesName, entitiesName, matrix, this);
    }
            
    private void normalizeRow (double [] row) {
        double max = -1.0, min = Double.MAX_VALUE;
        for (int i = 0; i < row.length; i++) {
            max = Math.max (max, row [i]);
            min = Math.min (min, row [i]);
        }
        
        double delta = max - min;
        for (int i = 0; i < row.length; i++) {
            row [i] = (row [i] - min) / delta;
        }
    }
    
    public List <String> getAllGenes () {
        return entitiesList.stream ()
             . map      (SourceEntity::getGenes)
             . flatMap  (Set::stream)
             . distinct ()
             . collect  (Collectors.toList ());
    }
    
}
