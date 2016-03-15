package org.reactome.server.analysis.service.model;


import org.reactome.server.analysis.core.model.identifier.InteractorIdentifier;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorEvidence extends IdentifierSummary {

    private String mapsTo;

    public InteractorEvidence(InteractorIdentifier interactorIdentifier) {
        super(interactorIdentifier.getId(), interactorIdentifier.getExp());
        this.mapsTo = interactorIdentifier.getMapsTo();
    }

    public String getMapsTo() {
        return mapsTo;
    }
}
