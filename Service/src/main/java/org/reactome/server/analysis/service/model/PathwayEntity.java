package org.reactome.server.analysis.service.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayEntity extends Identifier {

    private Set<IdentifierMap> mapsTo;

    public PathwayEntity(IdentifierSummary is, Set<IdentifierMap> mapsTo) {
        this.identifier = is.getId();
        this.exp = is.getExp();
        this.mapsTo = mapsTo;
    }

    public PathwayEntity(PathwayEntity pi, String resource){
        this.identifier = pi.identifier;
        this.exp = pi.exp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathwayEntity that = (PathwayEntity) o;

        //noinspection RedundantIfStatement
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }
}
