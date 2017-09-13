package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.model.resource.MainResource;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
//@ApiModel(value = "ReactionStatistics", description = "Statistics for a reaction type")
public class ReactionStatistics extends Statistics {

    public ReactionStatistics(PathwayNodeData d) {
        super("TOTAL", d.getReactionsCount(), d.getReactionsFound(), d.getReactionsRatio());
    }

    public ReactionStatistics(MainResource mainResource, PathwayNodeData d) {
        super(mainResource.getName(), d.getReactionsCount(mainResource), d.getReactionsFound(mainResource), d.getReactionsRatio(mainResource));
    }

}
