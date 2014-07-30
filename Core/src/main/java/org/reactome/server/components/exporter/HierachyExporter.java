package org.reactome.server.components.exporter;

import org.reactome.server.components.analysis.data.AnalysisData;
import org.reactome.server.components.analysis.model.PathwayHierarchy;
import org.reactome.server.components.analysis.model.PathwayNode;
import org.reactome.server.components.analysis.model.PathwayRoot;
import org.reactome.server.components.analysis.model.SpeciesNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class HierachyExporter {

    @Autowired
    private AnalysisData analysisData;

    public void exportParentship(){
        Map<SpeciesNode, PathwayHierarchy> hierarchies = analysisData.getPathwayHierarchies();
        for (SpeciesNode species : hierarchies.keySet()) {
            PathwayHierarchy hierarchy = hierarchies.get(species);
            for (PathwayRoot root : hierarchy.getChildren()) {
                printNodeParentship(root);
            }
        }
    }

    public void exportDetails(){
        Map<SpeciesNode, PathwayHierarchy> hierarchies = analysisData.getPathwayHierarchies();
        for (SpeciesNode species : hierarchies.keySet()) {
            PathwayHierarchy hierarchy = hierarchies.get(species);
            for (PathwayRoot root : hierarchy.getChildren()) {
                printNodeDetails(root);
            }
        }
    }

    private void printNodeParentship(PathwayNode node){
        for (PathwayNode child : node.getChildren()) {
            System.out.println(getNodeIdentifier(node) + "\t" + getNodeIdentifier(child));
            printNodeParentship(child);
        }
    }

    private void printNodeDetails(PathwayNode node){
        System.out.println(getNodeIdentifier(node) + "\t" + node.getName() + "\t" + node.getSpecies().getName());
        for (PathwayNode child : node.getChildren()) {
            printNodeDetails(child);
        }
    }

    private String getNodeIdentifier(PathwayNode node){
        String id = node.getStId();
        return id!=null && !id.isEmpty()? id : node.getPathwayId().toString();
    }
}
