package org.reactome.server.analysis.core.model;

import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.util.MapSet;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorNode {


    private MainIdentifier identifier;

    private Set<PhysicalEntityNode> interactsWith;

    //We DO NOT use PathwayNode here because at some point cloning the hierarchies
    //structure will be needed and keeping this separate will help to maintain the
    //links between both structures easy through the pathway location map
    private MapSet<Long, AnalysisReaction> pathwayReactions = null;

    public InteractorNode(MainResource resource, String accession) {
        this.interactsWith = new HashSet<>();
        this.identifier = new MainIdentifier(resource, new AnalysisIdentifier(accession));
    }

    public void addInteractsWith(PhysicalEntityNode interactsWith) {
        this.interactsWith.add(interactsWith);
    }

    public Set<PhysicalEntityNode> getInteractsWith() {
        return interactsWith;
    }

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
