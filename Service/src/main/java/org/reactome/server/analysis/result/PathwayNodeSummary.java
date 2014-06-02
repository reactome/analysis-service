package org.reactome.server.analysis.result;

import org.reactome.server.components.analysis.model.PathwayNode;
import org.reactome.server.components.analysis.model.PathwayNodeData;
import org.reactome.server.components.analysis.model.SpeciesNode;

/**
 * This class is based on PathwayNode but removing the tree hierarchy to store/retrieve the data
 * faster from/to the hard drive.
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayNodeSummary {
    private Long pathwayId;
    private Long diagramId;
    private String name;
    private SpeciesNode species;
    private PathwayNodeData data;


    public PathwayNodeSummary(PathwayNode node) {
        this.pathwayId = node.getPathwayId();
        this.diagramId = node.getDiagram().getPathwayId();
        this.name = node.getName();
        this.species = node.getSpecies();
        this.data = node.getPathwayNodeData();
    }

    public Long getPathwayId() {
        return pathwayId;
    }

    public Long getDiagramId() {
        return diagramId;
    }

    public String getName() {
        return name;
    }

    public PathwayNodeData getData() {
        return data;
    }

    public SpeciesNode getSpecies() {
        return species;
    }

}
