package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.identifier.MainIdentifier;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayInteractor {

    private String identifier;

    private String resource;

    private Set<Identifier> interactors;

    public PathwayInteractor(MainIdentifier identifier, Set<org.reactome.server.analysis.core.model.identifier.Identifier> interactors) {
        this.identifier = identifier.getValue().getId();
        this.resource = identifier.getResource().getName();
        this.interactors = new HashSet<>();
        for (org.reactome.server.analysis.core.model.identifier.Identifier interactor : interactors) {
            this.interactors.add(new Identifier(interactor.getValue().getId(), interactor.getValue().getExp()));
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getResource() {
        return resource;
    }

    public Set<Identifier> getInteractors() {
        return interactors;
    }
}
