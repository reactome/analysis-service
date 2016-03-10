package org.reactome.server.analysis.service.model;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class Identifier {

    String identifier;
    List<Double> exp;

    public Identifier() {
    }

    public Identifier(String identifier, List<Double> exp) {
        this.identifier = identifier;
        this.exp = exp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Double> getExp() {
        return exp;
    }
}
