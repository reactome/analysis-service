package org.reactome.server.analysis.core.model.identifier;

import org.reactome.server.analysis.core.model.AnalysisIdentifier;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorIdentifier extends AnalysisIdentifier {

    private String mapsTo;

    public InteractorIdentifier(String mapsTo) {
        super(mapsTo);
        this.mapsTo = mapsTo;
    }

    public InteractorIdentifier(AnalysisIdentifier identifier, String mapsTo) {
        this(identifier);
        this.mapsTo = mapsTo;
    }
    public InteractorIdentifier(AnalysisIdentifier identifier) {
        super(identifier.getId(), identifier.getExp());
    }

    public String getMapsTo() {
        return mapsTo;
    }
}
