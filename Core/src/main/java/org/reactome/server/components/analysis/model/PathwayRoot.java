package org.reactome.server.components.analysis.model;

import org.reactome.server.components.analysis.model.identifier.Identifier;
import org.reactome.server.components.analysis.model.identifier.MainIdentifier;

import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayRoot extends PathwayNode {
    private PathwayHierarchy pathwayHierarchy;

    public PathwayRoot(PathwayHierarchy pathwayHierarchy, String stId, Long pathwayId, String name, boolean hasDiagram) {
        super(stId, pathwayId, name, hasDiagram);
        this.pathwayHierarchy = pathwayHierarchy;
    }

    public PathwayHierarchy getPathwayHierarchy() {
        return pathwayHierarchy;
    }

    @Override
    public SpeciesNode getSpecies() {
        return pathwayHierarchy.getSpecies();
    }

    public void process(Identifier identifier, MainIdentifier mainIdentifier, Set<Long> reactions){
        super.process(identifier, mainIdentifier, reactions);
        this.pathwayHierarchy.process(identifier, mainIdentifier, reactions);
    }
}
