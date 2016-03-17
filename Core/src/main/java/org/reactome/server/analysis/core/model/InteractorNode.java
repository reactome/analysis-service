package org.reactome.server.analysis.core.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorNode {

    private String accession;

    private Set<PhysicalEntityNode> interactsWith;

    public InteractorNode(String accession) {
        this.interactsWith = new HashSet<>();
        this.accession = accession;
    }

    public void addInteractsWith(PhysicalEntityNode interactsWith) {
        this.interactsWith.add(interactsWith);
    }

    public String getAccession() {
        return accession;
    }

    public Set<PhysicalEntityNode> getInteractsWith() {
        return interactsWith;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InteractorNode that = (InteractorNode) o;

        return accession != null ? accession.equals(that.accession) : that.accession == null;

    }

    @Override
    public int hashCode() {
        return accession != null ? accession.hashCode() : 0;
    }
}
