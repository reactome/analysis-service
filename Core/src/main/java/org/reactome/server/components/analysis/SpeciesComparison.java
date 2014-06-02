package org.reactome.server.components.analysis;

import org.reactome.server.components.analysis.data.AnalysisData;
import org.reactome.server.components.analysis.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class SpeciesComparison {

    @Autowired
    private AnalysisData analysisData;

    @Autowired
    private EnrichmentAnalysis enrichmentAnalysis;

    public HierarchiesData speciesComparison(SpeciesNode speciesFrom, SpeciesNode speciesTo){
        UserData ud = getSyntheticUserData(speciesFrom);
        return enrichmentAnalysis.overRepresentation(ud.getIdentifiers(), speciesTo);
    }

    public UserData getSyntheticUserData(SpeciesNode species){
        PhysicalEntityGraph graph = analysisData.getPhysicalEntityGraph();

        Set<AnalysisIdentifier> speciesToIdentifiers = new HashSet<AnalysisIdentifier>();
        for (PhysicalEntityNode node : graph.getAllNodes()) {
            if(species.equals(node.getSpecies())){
                if(node.getIdentifier()!=null){
                    speciesToIdentifiers.add(node.getIdentifier().getValue());
                }
            }
        }
        return new UserData(new LinkedList<String>(), speciesToIdentifiers, null);
    }

    @Deprecated
    public UserData getSyntheticUserData(SpeciesNode speciesFrom, SpeciesNode speciesTo){
        PhysicalEntityGraph graph = analysisData.getPhysicalEntityGraph();

        Set<AnalysisIdentifier> speciesToIdentifiers = new HashSet<AnalysisIdentifier>();
        for (PhysicalEntityNode node : graph.getAllNodes()) {
            if(speciesFrom.equals(node.getSpecies())){
                PhysicalEntityNode eq = node.getProjection(speciesTo);
                if(eq!=null && node.getIdentifier()!=null){
                    speciesToIdentifiers.add(node.getIdentifier().getValue());
                }
            }
        }
        return new UserData(new LinkedList<String>(), speciesToIdentifiers, null);
    }
}
