package org.reactome.server.analysis.tools.components.exporter;

import org.gk.persistence.MySQLAdaptor;
import org.reactome.core.controller.DatabaseObjectHelper;
import org.reactome.core.factory.DatabaseObjectFactory;
import org.reactome.core.model.ReactionlikeEvent;
import org.reactome.core.model.Species;
import org.reactome.core.model.StableIdentifier;
import org.reactome.server.analysis.core.data.AnalysisData;
import org.reactome.server.analysis.core.model.AnalysisReaction;
import org.reactome.server.analysis.core.model.PhysicalEntityNode;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.ResourceFactory;
import org.reactome.server.analysis.core.util.MapSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class ReactionLevelExporter {

    final static String pbUrl = "http://www.reactome.org/content/detail/";

    @Autowired
    private AnalysisData analysisData;

    @Autowired
    private DatabaseObjectHelper helper;


    @SuppressWarnings("ConstantConditions")
    public void export(MySQLAdaptor dba, ResourceFactory.MAIN resource, String fileName) throws FileNotFoundException {
        System.out.println("ReactionLevelExporter");

        //THIS IS NEEDED HERE
        this.helper.setDba(dba);

        MapSet<String, Long> resourceToReactions = new MapSet<>();

        for (PhysicalEntityNode node : analysisData.getPhysicalEntityGraph().getAllNodes()) {
            MainIdentifier identifier = node.getIdentifier();
            if (identifier != null && identifier.is(resource)) {
                resourceToReactions.addAll(getResourceToReaction(identifier.getValue().getId(), node));
            }
        }

        //Sorting the list to make it more professional xDD
        List<String> identifiers = new LinkedList<>(resourceToReactions.keySet());
        Collections.sort(identifiers);

        PrintStream ps = new PrintStream(new FileOutputStream(new File(fileName)));
        for (String identifier : identifiers) {
            for (Long reactionDbId : resourceToReactions.getElements(identifier)) {
                ps.println(generateLink(reactionDbId, identifier));
            }
        }
        ps.close();
    }

    private MapSet<String, Long> getResourceToReaction(String identifier, PhysicalEntityNode node) {
        MapSet<String, Long> rtn = new MapSet<>();
        for (Long pathway : node.getPathwayIds()) {
            for (AnalysisReaction analysisReaction : node.getReactions(pathway)) {
                rtn.add(identifier, analysisReaction.getDbId());
            }
        }

        for (PhysicalEntityNode parent : node.getParents()) {
            rtn.addAll(getResourceToReaction(identifier, parent));
        }
        return rtn;
    }

    private String generateLink(Long reactionId, String resource) {
        ReactionlikeEvent reaction = DatabaseObjectFactory.getDatabaseObject(reactionId);
        Species species = DatabaseObjectFactory.getDatabaseObject(reaction.getSpecies().get(0).getDbId());

        //Resource based Identifier
        StringBuilder sb = new StringBuilder(resource);

        //LINK
        StableIdentifier stableIdentifier = reaction.getStableIdentifier().load();
        sb.append("\t").append(pbUrl).append(stableIdentifier.getIdentifier());

        //Pathway name
        String name = reaction.getDisplayName();

        sb.append("\t").append(name);

        //GO Evidence Codes
        //TAS: Traceable Author Statement
        //IEA: Inferred from Electronic Annotation
        sb.append("\t").append(reaction.getIsInferred() ? "IEA" : "TAS");

        //Species name
        sb.append("\t").append(species.getDisplayName());

        return sb.toString();
    }
}