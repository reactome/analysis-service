package org.reactome.server.analysis.service.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class FoundEntity extends IdentifierSummary {

    private Set<IdentifierMap> mapsTo;

    public FoundEntity(IdentifierSummary is, Set<IdentifierMap> mapsTo) {
        super(is.getId(), is.getExp());

        this.mapsTo = mapsTo;
    }

    public FoundEntity(FoundEntity pi, String resource){
        super(pi.getId(), pi.getExp());
        this.mapsTo = new HashSet<>();
        for (IdentifierMap identifierMap : pi.mapsTo) {
            if(identifierMap.getResource().equals(resource)){
                this.mapsTo.add(identifierMap);
            }
        }
    }

    public Set<IdentifierMap> getMapsTo() {
        return mapsTo;
    }

    protected void merge(Set<IdentifierMap> mapsTo){
        for (IdentifierMap aux : mapsTo) {
            boolean added = false;
            for (IdentifierMap mt : this.mapsTo) {
                if(mt.getResource().equals(aux.getResource())){
                    mt.addAll(aux.getIds());
                    added = true;
                    break;
                }
            }
            if(!added){
                this.mapsTo.add(aux);
            }
        }
    }
}
