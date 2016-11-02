package org.reactome.server.analysis.core.model;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class SpeciesNodeFactory {

    public static long HUMAN_DB_ID = 48887L;
    public static String HUMAN_TAX_ID = "9606";
    public static String HUMAN_STR = "Homo sapiens";

    private static Map<Long, SpeciesNode> speciesMap = new HashMap<Long, SpeciesNode>();

    public static SpeciesNode getSpeciesNode(Long speciesID, String taxID, String name) {
        SpeciesNode speciesNode = speciesMap.get(speciesID);
        if (speciesNode == null) {
            speciesNode = new SpeciesNode(speciesID, taxID, name);
            speciesMap.put(speciesID, speciesNode);
        }
        return speciesNode;
    }

    public static SpeciesNode getSpeciesNode(GKInstance s) {
        SpeciesNode speciesNode = speciesMap.get(s.getDBID());
        if (speciesNode == null) {
            try {
                GKInstance taxon = (GKInstance) s.getAttributeValue(ReactomeJavaConstants.crossReference);
                String taxId = (taxon != null) ? (String) taxon.getAttributeValue(ReactomeJavaConstants.identifier) : null;
                speciesNode = new SpeciesNode(s.getDBID(), taxId, s.getDisplayName());
                speciesMap.put(s.getDBID(), speciesNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return speciesNode;
    }

    public static SpeciesNode getHumanNode() {
        return new SpeciesNode(HUMAN_DB_ID, HUMAN_TAX_ID, HUMAN_STR);
    }
}
