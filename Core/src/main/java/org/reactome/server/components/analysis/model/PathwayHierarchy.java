package org.reactome.server.components.analysis.model;

import org.reactome.server.components.analysis.model.identifier.Identifier;
import org.reactome.server.components.analysis.model.identifier.MainIdentifier;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayHierarchy implements Serializable {
    private Set<PathwayRoot> children;
    private SpeciesNode species;

    private PathwayNodeData data;

    public PathwayHierarchy(SpeciesNode species) {
        this.species = species;
        this.children = new HashSet<PathwayRoot>();
        this.data = new PathwayNodeData();
    }

    public PathwayNode addFrontpageItem(Long pathwayId, String name, boolean hasDiagram){
        PathwayRoot node = new PathwayRoot(this, pathwayId, name, hasDiagram);
        this.children.add(node);
        return node;
    }

    public Set<PathwayRoot> getChildren() {
        return children;
    }

    public PathwayNodeData getData() {
        return data;
    }

    public SpeciesNode getSpecies() {
        return species;
    }

    protected Set<PathwayNode> getHitPathways(){
        Set<PathwayNode> rtn = new HashSet<PathwayNode>();
        for (PathwayNode node : this.children) {
            rtn.addAll(node.getHitNodes());
        }
        return rtn;
    }

    public void setCountersAndCleanUp(){
        this.data.setCounters();
        for (PathwayRoot node : children) {
            node.setCounters();
        }
    }

    public void process(Identifier identifier, MainIdentifier mainIdentifier, Set<Long> reactions){
        this.data.addMapping(identifier, mainIdentifier);
        this.data.addReactions(mainIdentifier.getResource(), reactions);
    }
}
