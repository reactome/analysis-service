package org.reactome.server.analysis.tools.components.filter;

import org.reactome.server.analysis.core.model.IdentifiersMap;
import org.reactome.server.analysis.core.model.InteractorNode;
import org.reactome.server.analysis.core.model.PhysicalEntityGraph;
import org.reactome.server.analysis.core.model.PhysicalEntityNode;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.model.resource.ResourceFactory;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.analysis.tools.BuilderTool;
import org.reactome.server.interactors.database.InteractorsDatabase;
import org.reactome.server.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.interactors.model.Interaction;
import org.reactome.server.interactors.model.Interactor;
import org.reactome.server.interactors.model.InteractorResource;
import org.reactome.server.interactors.service.InteractionService;
import org.reactome.server.interactors.service.InteractorResourceService;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class InteractorsBuilder {

    private static final String STATIC = "static";

    private InteractionService interactionService;

    //Will contain the RADIX-TREE with the map (identifiers -> [InteractorNode])
    private IdentifiersMap<InteractorNode> interactorsMap = new IdentifiersMap<>();

    public void build(PhysicalEntityGraph entities, InteractorsDatabase interactorsDatabase) {
//        InteractorService interactorService = new InteractorService(interactorsDatabase);
        this.interactionService = new InteractionService(interactorsDatabase);
        InteractorResourceService interactorResourceService = new InteractorResourceService(interactorsDatabase);

        Map<Long, InteractorResource> resourceMap;
        try {
            resourceMap = interactorResourceService.getAllMappedById();
        } catch (SQLException e) {
            System.err.println("Interactor Resource Map couldn't be loaded");
            return;
        }

        Set<PhysicalEntityNode> allNodes = entities.getAllNodes();
        int i = 0, tot = allNodes.size();
        for (PhysicalEntityNode entityNode : allNodes) {
            if(BuilderTool.VERBOSE) {
                System.out.print("\rInteractors for -> " + entityNode.getId() + " >> " + (++i) + "/" + tot);
            }
            if(!entityNode.isDirectlyInADiagram()) continue;
            MainIdentifier identifier = entityNode.getIdentifier();
            if (identifier != null) {
                String acc = identifier.getValue().getId();
                for (Interactor interactor : getInteractors(acc)) {
                    InteractorResource aux = resourceMap.get(interactor.getInteractorResourceId());
                    Resource resource = ResourceFactory.getResource(aux.getName());
                    InteractorNode interactorNode = getOrCreate(resource, interactor.getAcc());
                    interactorNode.addInteractsWith(entityNode);
                    interactorsMap.add(interactor.getAlias(), resource, interactorNode);
                    interactorsMap.add(interactor.getAliasWithoutSpecies(false), resource, interactorNode);
                    //for (String synonym : interactor.getSynonyms().split("\\$")) interactorsMap.add(synonym, resource, interactorNode);
                }
            }
        }
        if(BuilderTool.VERBOSE){
            System.out.println("\r" + n + " interactors successfully added to Reactome");
        }
    }

    public IdentifiersMap<InteractorNode> getInteractorsMap() {
        return interactorsMap;
    }

    private List<Interactor> getInteractors(String acc) {
        List<Interactor> rtn = new ArrayList<>();
        List<Interaction> interactions;
        try {
            interactions = interactionService.getInteractions(acc, STATIC);
        } catch (InvalidInteractionResourceException | SQLException e) {
            return rtn;
        }
        for (Interaction interaction : interactions) {
            rtn.add(interaction.getInteractorB());
        }
        return rtn;
    }

    private int n = 0;
    private InteractorNode getOrCreate(Resource resource, String identifier){
        MapSet<Resource, InteractorNode> map = interactorsMap.get(identifier);
        Set<InteractorNode> interactors = map.getElements(resource);
        if(interactors == null || interactors.isEmpty()) {
            InteractorNode interactorNode = new InteractorNode(identifier);
            interactorsMap.add(identifier, resource, interactorNode);
            n++;
            return interactorNode;
        } else {
            //Using IdentifiersMap causes this "oddity" here, but is a minor inconvenient
            if (interactors.size() > 1)
                System.err.println("Interactors duplication. There should not be more than one interactor for " + identifier + " [" + resource.getName() + "]");
            return interactors.iterator().next();
        }
    }
}
