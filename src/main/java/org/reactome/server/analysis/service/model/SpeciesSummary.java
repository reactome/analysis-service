package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.SpeciesNode;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class SpeciesSummary {
    private Long dbId;
    private String taxId;
    private String name;

    public SpeciesSummary(SpeciesNode species) {
        this.dbId = species.getSpeciesID();
        this.taxId = species.getTaxID();
        this.name = species.getName();
    }

    public Long getDbId() {
        return dbId;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getName() {
        return name;
    }
}
