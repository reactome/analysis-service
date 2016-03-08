package org.reactome.server.analysis.tools.components.filter;

import org.reactome.server.analysis.core.model.IdentifiersMap;
import org.reactome.server.analysis.core.model.InteractorNode;
import org.reactome.server.analysis.core.model.PhysicalEntityGraph;
import org.reactome.server.analysis.core.model.PhysicalEntityNode;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.model.resource.ResourceFactory;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.tools.interactors.database.InteractorsDatabase;
import org.reactome.server.tools.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.tools.interactors.model.Interaction;
import org.reactome.server.tools.interactors.model.Interactor;
import org.reactome.server.tools.interactors.model.InteractorResource;
import org.reactome.server.tools.interactors.service.InteractionService;
import org.reactome.server.tools.interactors.service.InteractorResourceService;
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

        for (PhysicalEntityNode entityNode : entities.getAllNodes()) {
            MainIdentifier identifier = entityNode.getIdentifier();
            if (identifier != null) {
                String acc = identifier.getValue().getId();
                for (Interactor interactor : getInteractors(acc)) {
                    InteractorResource aux = resourceMap.get(interactor.getInteractorResourceId());
                    Resource resource = ResourceFactory.getResource(aux.getName().replace("KB", ""));
                    if (resource instanceof MainResource) {
                        InteractorNode interactorNode = getOrCreate((MainResource) resource, interactor.getAcc());
                        interactorNode.addInteractsWith(entityNode);
                        interactorsMap.add(interactor.getAlias(), resource, interactorNode);
                        interactorsMap.add(interactor.getAliasWithoutSpecies(false), resource, interactorNode);
                        for (String synonym : interactor.getSynonyms().split("\\$")) {
                            interactorsMap.add(synonym, resource, interactorNode);
                        }
                    } else {
                        System.err.println("Not know resource for: " + interactor);
                    }
                }
            }
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

    private InteractorNode getOrCreate(MainResource resource, String identifier){
        MapSet<Resource, InteractorNode> map = interactorsMap.get(identifier);
        Set<InteractorNode> interactors = map.getElements(resource);
        if (interactors.isEmpty()) {
            InteractorNode interactorNode = new InteractorNode(resource, identifier);
            interactorsMap.add(identifier, resource, interactorNode);
            return interactorNode;
        } else {
            //Using IdentifiersMap causes this "oddity" here, but is a minor inconvenient
            if (interactors.size() > 1)
                System.err.println("Interactors duplication. There should not be more than one interactor for " + identifier + " [" + resource.getName() + "]");
            return interactors.iterator().next();
        }
    }
}
