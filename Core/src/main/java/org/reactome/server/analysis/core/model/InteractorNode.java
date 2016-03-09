package org.reactome.server.analysis.core.model;

import org.reactome.server.analysis.core.model.identifier.Identifier;
import org.reactome.server.analysis.core.model.identifier.OtherIdentifier;
import org.reactome.server.analysis.core.model.resource.Resource;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorNode {

    private Identifier identifier;

    private Set<PhysicalEntityNode> interactsWith;

    //We DO NOT use PathwayNode here because at some point cloning the hierarchies
    //structure will be needed and keeping this separate will help to maintain the
    //links between both structures easy through the pathway location map
//    private MapSet<Long, AnalysisReaction> pathwayReactions;

    public InteractorNode(Resource resource, String accession) {
        this.interactsWith = new HashSet<>();
        this.identifier = new OtherIdentifier(resource, new AnalysisIdentifier(accession));
//        this.pathwayReactions = pathwayReactions;
    }

    public void addInteractsWith(PhysicalEntityNode interactsWith) {
        this.interactsWith.add(interactsWith);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Set<PhysicalEntityNode> getInteractsWith() {
        return interactsWith;
    }

//    public MapSet<Long, AnalysisReaction> getPathwayReactions() {
//        return pathwayReactions;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InteractorNode that = (InteractorNode) o;

        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }
}
