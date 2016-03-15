package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.identifier.InteractorIdentifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayInteractor {

    private String identifier;

    private String resource;

    private Set<InteractorEvidence> interactors;

    public PathwayInteractor(MainIdentifier identifier, Set<InteractorIdentifier> interactors) {
        this.identifier = identifier.getValue().getId();
        this.resource = identifier.getResource().getName();
        this.interactors = new HashSet<>();
        for (InteractorIdentifier interactor : interactors) {
            this.interactors.add(new InteractorEvidence(interactor));
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getResource() {
        return resource;
    }

    public Set<InteractorEvidence> getInteractors() {
        return interactors;
    }
}
