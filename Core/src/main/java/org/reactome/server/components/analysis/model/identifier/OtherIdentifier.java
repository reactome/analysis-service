package org.reactome.server.components.analysis.model.identifier;

import org.reactome.server.components.analysis.model.AnalysisIdentifier;
import org.reactome.server.components.analysis.model.resource.Resource;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class OtherIdentifier extends Identifier<Resource> {

    public OtherIdentifier(Resource resource, AnalysisIdentifier identifier) {
        super(resource, identifier);
    }

}
