package org.reactome.server.components.analysis.filter;

import org.gk.persistence.MySQLAdaptor;
import org.reactome.server.components.analysis.data.AnalysisDataUtils;
import org.reactome.server.components.analysis.model.*;
import org.reactome.server.components.analysis.model.identifier.MainIdentifier;
import org.reactome.server.components.analysis.util.MapSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("UnusedDeclaration")
@Component
public class AnalysisBuilder {
    @Autowired
    private PathwayHierarchyBuilder pathwaysBuilder;
    @Autowired
    private ReactionLikeEventBuilder rleBuilder;
    @Autowired
    private PhysicalEntityHierarchyBuilder peBuilder;

    public void build(MySQLAdaptor dba, String fileName){
        this.pathwaysBuilder.build(dba);
        this.rleBuilder.build(dba, this.pathwaysBuilder.getPathwayLocation());
        this.peBuilder.build(dba, rleBuilder.getEntityPathwayReaction());
        this.peBuilder.setOrthologous();
//        this.orthologyBuilder.build(dba, peBuilder.getPhysicalEntityGraph(), peBuilder.getPhysicalEntityBuffer());
        //Pre-calculates the counters for each MainResource/PathwayNode
        this.calculateNumbersInHierarchyNodesForMainResources();

        DataContainer container = new DataContainer(getHierarchies(),
                                                    getPhysicalEntityGraph(),
                                                    getPathwayLocation(),
                                                    getIdentifierMap());
        AnalysisDataUtils.kryoSerialisation(container, fileName);
    }

    private void calculateNumbersInHierarchyNodesForMainResources(){
        MapSet<Long, PathwayNode> pathwayLocation = getPathwayLocation();
        IdentifiersMap identifiersMap = this.getIdentifierMap();

        for (PhysicalEntityNode physicalEntityNode : getPhysicalEntityGraph().getAllNodes()) {
            MainIdentifier mainIdentifier = physicalEntityNode.getIdentifier();
            if(mainIdentifier!=null){
                for (Long pathwayId : physicalEntityNode.getPathwayIds()) {
                    Set<PathwayNode> pNodes = pathwayLocation.getElements(pathwayId);
                    if(pNodes==null) continue;
                    for (PathwayNode pathwayNode : pNodes) {
                        Set<Long> reactions = physicalEntityNode.getReactions(pathwayId);
                        pathwayNode.process(mainIdentifier, reactions);
                    }
                }
            }
        }
        pathwaysBuilder.prepareToSerialise();
    }

    public Map<SpeciesNode, PathwayHierarchy> getHierarchies() {
        return pathwaysBuilder.getHierarchies();
    }

    public IdentifiersMap getIdentifierMap(){
        return peBuilder.getIdentifiersMap();
    }

    public MapSet<Long, PathwayNode> getPathwayLocation(){
        return pathwaysBuilder.getPathwayLocation();
    }

    public PhysicalEntityGraph getPhysicalEntityGraph() {
        return peBuilder.getPhysicalEntityGraph();
    }
}
