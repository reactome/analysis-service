package org.reactome.server.analysis.core.components;

import org.apache.log4j.Logger;
import org.reactome.server.analysis.core.data.AnalysisData;
import org.reactome.server.analysis.core.data.HierarchiesDataContainer;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.model.identifier.Identifier;
import org.reactome.server.analysis.core.model.identifier.InteractorIdentifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.identifier.OtherIdentifier;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.util.MapSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class EnrichmentAnalysis {

    private static Logger logger = Logger.getLogger(EnrichmentAnalysis.class.getName());

    public static final Object ANALYSIS_SEMAPHORE = new Object();
    private static long ANALYSIS_COUNT = 0L;

    @Autowired
    private AnalysisData analysisData;

    public HierarchiesData overRepresentation(Set<AnalysisIdentifier> identifiers, SpeciesNode speciesNode, boolean includeInteractors) {
        this.increaseCounter();
        HierarchiesData hierarchiesData = HierarchiesDataContainer.take();
        this.analyse(hierarchiesData, identifiers, speciesNode, includeInteractors);
        this.decreaseCounter();
        return hierarchiesData;
    }

    public static long getAnalysisCount() {
        return ANALYSIS_COUNT;
    }

    private void analyse(HierarchiesData hierarchies, Set<AnalysisIdentifier> identifiers, SpeciesNode speciesNode, boolean includeInteractors) {
        Integer originalSampleSize = identifiers.size();
        IdentifiersMap<PhysicalEntityNode> entitiesMap = analysisData.getEntitiesMap();
        IdentifiersMap<InteractorNode> interactorsMap = analysisData.getInteractorsMap();

        logger.trace(String.format("Analysing %d identifiers%s%s...", originalSampleSize, includeInteractors ? " | including interactors " : "|" , speciesNode != null ? " projecting to " + speciesNode.getName() : ""));
        long start = System.currentTimeMillis();

        Set<MainIdentifier> newSample = new HashSet<>();
        for (AnalysisIdentifier identifier : identifiers) {
            MapSet<Resource, PhysicalEntityNode> resourceEntities = entitiesMap.get(identifier);
//            if (resourceEntities.isEmpty()) {
//                hierarchies.addNotFound(identifier);
//                continue;
//            }
            boolean found = false;
            for (Resource resource : resourceEntities.keySet()) {
                Identifier otherIdentifier = new OtherIdentifier(resource, identifier);
                for (PhysicalEntityNode node : resourceEntities.getElements(resource)) {
                    if (speciesNode != null) node = node.getProjection(speciesNode);
                    if (node == null) continue;
                    found = true;
                    MainIdentifier mainAux = node.getIdentifier();
                    if (mainAux != null) {
                        //Create a copy of the main identifier and add to it the expression values of the analysed one
                        AnalysisIdentifier ai = new AnalysisIdentifier(mainAux.getValue().getId(), otherIdentifier.getValue().getExp());
                        MainIdentifier mainIdentifier = new MainIdentifier(mainAux.getResource(), ai);
                        for (Long pathwayId : node.getPathwayIds()) {
                            Set<PathwayNode> pNodes = hierarchies.getPathwayLocation().getElements(pathwayId);
                            if (pNodes == null) continue;
                            for (PathwayNode pNode : pNodes) {
                                Set<AnalysisReaction> reactions = node.getReactions(pathwayId);
                                pNode.process(otherIdentifier, mainIdentifier, reactions);
                                newSample.add(mainIdentifier);
                            }
                        }
                    }
                }
            }

            if (includeInteractors) {
                MapSet<Resource, InteractorNode> interactors = interactorsMap.get(identifier);
                for (Resource resource : interactors.keySet()) {
                    //Note: It goes only once
                    for (InteractorNode interactorNode : interactors.getElements(resource)) {
                        InteractorIdentifier interactorIdentifier = new InteractorIdentifier(identifier, interactorNode.getAccession());
                        for (PhysicalEntityNode node : interactorNode.getInteractsWith()) {
                            //When there is projection, for the time being the hit interactors for other species are not
                            //taken into account (please read comment bellow the following line)
                            if(speciesNode != null && !node.getSpecies().equals(speciesNode)) continue;
                            //TODO: Projection for the interactors cannot be done until the orthologs are included in the interactors-core
                            //if (speciesNode != null) node = node.getProjection(speciesNode);
                            //if (node == null) continue;
                            found = true;
                            MainIdentifier mainAux = node.getIdentifier();
                            if (mainAux != null) {
                                //Create a copy of the main identifier and add to it the expression values of the analysed one
                                AnalysisIdentifier ai = new AnalysisIdentifier(mainAux.getValue().getId(), interactorIdentifier.getExp());
                                MainIdentifier mainIdentifier = new MainIdentifier(mainAux.getResource(), ai);
                                for (Long pathwayId : node.getPathwayIds()) {
                                    Set<PathwayNode> pNodes = hierarchies.getPathwayLocation().getElements(pathwayId);
                                    if (pNodes == null) continue;
                                    for (PathwayNode pNode : pNodes) {
                                        Set<AnalysisReaction> reactions = node.getReactions(pathwayId, false);
                                        pNode.processInteractor(interactorIdentifier, mainIdentifier, reactions);
                                        newSample.add(mainIdentifier);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!found) {
                hierarchies.addNotFound(identifier);
            }
        }
        //IMPORTANT: For the statistics the sample is the projection we find (newSample) plus the not found identifiers
        //           in the original sample
        Integer finalSampleSize = newSample.size() + hierarchies.getNotFound().size();
        Map<MainResource, Integer> sampleSizePerResource = new HashMap<MainResource, Integer>();
        for (MainIdentifier mainIdentifier : newSample) {
            Integer aux = sampleSizePerResource.get(mainIdentifier.getResource());
            sampleSizePerResource.put(mainIdentifier.getResource(), aux == null ? 1 : aux + 1);
        }

        logger.trace(String.format("Final sample size is %d identifiers", finalSampleSize));
        hierarchies.setResultStatistics(sampleSizePerResource, hierarchies.getNotFound().size(), includeInteractors);
        long end = System.currentTimeMillis();
        logger.trace(String.format("Analysis for %d identifiers performed in %d ms", finalSampleSize, end - start));
    }

    private void decreaseCounter() {
        synchronized (ANALYSIS_SEMAPHORE) {
            if (--ANALYSIS_COUNT == 0) {
                ANALYSIS_SEMAPHORE.notify(); //Only one background producer should be using this semaphore
            }
        }
    }

    private void increaseCounter() {
        synchronized (ANALYSIS_SEMAPHORE) {
            ++ANALYSIS_COUNT;
        }
    }
}
