package org.reactome.server.analysis.service.model;

import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class FoundInteractor extends IdentifierSummary {

    private Set<String> mapsTo;
    private IdentifierMap interactsWith;

    public FoundInteractor(IdentifierSummary is, Set<String> mapsTo, IdentifierMap interactsWith) {
        super(is.getId(), is.getExp());

        this.mapsTo = mapsTo;
        this.interactsWith = interactsWith;
    }

    public Set<String> getMapsTo() {
        return mapsTo;
    }

    public IdentifierMap getInteractsWith() {
        return interactsWith;
    }
}
