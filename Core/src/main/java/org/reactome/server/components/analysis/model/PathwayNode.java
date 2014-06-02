package org.reactome.server.components.analysis.model;

import org.reactome.server.components.analysis.model.identifier.Identifier;
import org.reactome.server.components.analysis.model.identifier.MainIdentifier;
import org.reactome.server.components.analysis.model.resource.MainResource;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayNode implements Serializable, Comparable<PathwayNode> {

    private Long pathwayId;
    private String name;
    private boolean hasDiagram;

    private PathwayNode parent;
    private Set<PathwayNode> children;

    private PathwayNodeData data;

    public PathwayNode(Long pathwayId, String name, boolean hasDiagram) {
        this(null, pathwayId, name, hasDiagram);
    }

    protected PathwayNode(PathwayNode parent, Long pathwayId, String name, boolean hasDiagram) {
        this.parent = parent;
        this.pathwayId = pathwayId;
        this.name = name;
        this.hasDiagram = hasDiagram;
        this.children = new HashSet<PathwayNode>();
        this.data = new PathwayNodeData();
    }

    public PathwayNode addChild(Long pathwayId, String name, boolean hasDiagram){
        PathwayNode node = new PathwayNode(this, pathwayId, name, hasDiagram);
        this.children.add(node);
        return node;
    }

    public String getName() {
        return name;
    }

    public PathwayNode getDiagram(){
        if(this.hasDiagram){
            return this;
        }
        if(parent != null){
            return parent.getDiagram();
        }
        return null;
    }

    protected Set<PathwayNode> getHitNodes(){
        Set<PathwayNode> rtn = new HashSet<PathwayNode>();
        if(this.data.hasResult()){
            rtn.add(this);
            for (PathwayNode node : children) {
                rtn.addAll(node.getHitNodes());
            }
        }
        return rtn;
    }

    public Long getPathwayId() {
        return pathwayId;
    }

    public PathwayNodeData getPathwayNodeData() {
        return data;
    }

    public SpeciesNode getSpecies(){
        if(this.parent==null){
            PathwayRoot root = (PathwayRoot) this;
            return root.getSpecies();
        }else{
            return parent.getSpecies();
        }
    }

    protected void setCounters(){
        this.data.setCounters();
        for (PathwayNode child : this.children) {
            child.setCounters();
        }
    }

    public void setResultStatistics(PathwayNodeData speciesData, Map<MainResource, Integer> sampleSizePerResource, Integer notFound){
        for (PathwayNode child : this.children) {
            child.setResultStatistics(speciesData, sampleSizePerResource, notFound);
        }
        this.data.setResultStatistics(speciesData, sampleSizePerResource, notFound);
    }

    public void process(MainIdentifier mainIdentifier, Set<Long> reactions){
        this.process(mainIdentifier, mainIdentifier, reactions);
    }

    public void process(Identifier identifier, MainIdentifier mainIdentifier, Set<Long> reactions){
        this.data.addMapping(identifier, mainIdentifier);
        this.data.addReactions(mainIdentifier.getResource(), reactions);
        if(this.parent!=null){
            this.parent.process(identifier, mainIdentifier, reactions);
        }
    }

    @Override
    public int compareTo(PathwayNode o) {
        return this.data.getEntitiesPValue().compareTo(o.data.getEntitiesPValue());
//        return this.name.compareToIgnoreCase(o.name);
    }
}
