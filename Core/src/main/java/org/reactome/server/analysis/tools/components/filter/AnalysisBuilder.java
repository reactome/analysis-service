package org.reactome.server.analysis.tools.components.filter;

import org.gk.persistence.MySQLAdaptor;
import org.reactome.server.analysis.core.data.AnalysisDataUtils;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.model.identifier.InteractorIdentifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.tools.interactors.database.InteractorsDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class AnalysisBuilder {
    @Autowired
    private PathwayHierarchyBuilder pathwaysBuilder;
    @Autowired
    private ReactionLikeEventBuilder rleBuilder;
    @Autowired
    private PhysicalEntityHierarchyBuilder peBuilder;
    @Autowired
    private InteractorsBuilder interactorsBuilder;

    public void build(MySQLAdaptor dba, InteractorsDatabase interactorsDatabase, String fileName){
        this.pathwaysBuilder.build(dba);
        this.rleBuilder.build(dba, this.pathwaysBuilder.getPathwayLocation());
        this.peBuilder.build(dba, rleBuilder.getEntityPathwayReaction());
        this.interactorsBuilder.build(peBuilder.getPhysicalEntityGraph(), interactorsDatabase);
        this.peBuilder.setOrthologous();
        //Pre-calculates the counters for each MainResource/PathwayNode
        this.calculateNumbersInHierarchyNodesForMainResources();

        DataContainer container = new DataContainer(getHierarchies(),
                                                    getPhysicalEntityGraph(),
                                                    getPathwayLocation(),
                                                    getEntitiesMap(),
                                                    getInteractorsMap());
        AnalysisDataUtils.kryoSerialisation(container, fileName);
    }

    private void calculateNumbersInHierarchyNodesForMainResources(){
        MapSet<Long, PathwayNode> pathwayLocation = getPathwayLocation();

        for (PhysicalEntityNode physicalEntityNode : getPhysicalEntityGraph().getAllNodes()) {
            MainIdentifier mainIdentifier = physicalEntityNode.getIdentifier();
            if (mainIdentifier != null) {
                for (Long pathwayId : physicalEntityNode.getPathwayIds()) {
                    Set<PathwayNode> pNodes = pathwayLocation.getElements(pathwayId);
                    if (pNodes == null) continue;
                    for (PathwayNode pathwayNode : pNodes) {
                        Set<AnalysisReaction> reactions = physicalEntityNode.getReactions(pathwayId);
                        pathwayNode.process(mainIdentifier, reactions);
                    }
                }
            }
        }

        for (InteractorNode interactorNode : getInteractorNodes()) {
            InteractorIdentifier identifier = new InteractorIdentifier(interactorNode.getAccession());
            for (PhysicalEntityNode physicalEntityNode : interactorNode.getInteractsWith()) {
                MainIdentifier mainIdentifier = physicalEntityNode.getIdentifier();
                if (mainIdentifier != null) {
                    for (Long pathwayId : physicalEntityNode.getPathwayIds()) {
                        Set<PathwayNode> pNodes = pathwayLocation.getElements(pathwayId);
                        if (pNodes == null) continue;
                        for (PathwayNode pathwayNode : pNodes) {
                            Set<AnalysisReaction> reactions = physicalEntityNode.getReactions(pathwayId, false);
                            pathwayNode.processInteractor(identifier, mainIdentifier, reactions);
                        }
                    }
                }
            }
        }

        pathwaysBuilder.prepareToSerialise();
    }

    public Map<SpeciesNode, PathwayHierarchy> getHierarchies() {
        return pathwaysBuilder.getHierarchies();
    }

    public IdentifiersMap<PhysicalEntityNode> getEntitiesMap(){
        return peBuilder.getEntitiesMap();
    }

    public IdentifiersMap<InteractorNode> getInteractorsMap(){
        return this.interactorsBuilder.getInteractorsMap();
    }

    public MapSet<Long, PathwayNode> getPathwayLocation(){
        return pathwaysBuilder.getPathwayLocation();
    }

    public PhysicalEntityGraph getPhysicalEntityGraph() {
        return peBuilder.getPhysicalEntityGraph();
    }

    public Set<InteractorNode> getInteractorNodes() {
        return interactorsBuilder.getInteractorsMap().values();
    }
}
