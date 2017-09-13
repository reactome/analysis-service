package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.identifier.InteractorIdentifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.analysis.service.result.PathwayNodeSummary;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class FoundInteractors {

    private List<FoundInteractor> identifiers;
    private Set<String> resources;
    private List<String> expNames;
    private Integer found;

    private FoundInteractors(List<FoundInteractor> interactors, Set<String> resources, List<String> expNames) {
        this.identifiers = interactors;
        this.resources = resources;
        this.expNames = expNames;
        this.found = identifiers.size();
    }

    public FoundInteractors(PathwayNodeSummary nodeSummary, List<String> expNames) {
        this.expNames = expNames;

        this.resources = new HashSet<>();
        this.identifiers = new LinkedList<>();

        MapSet<Resource, IdentifierSummary> summaries = new MapSet<>();
        MapSet<IdentifierSummary, String> interactsWith = new MapSet<>();
        MapSet<IdentifierSummary, String> mapsTo = new MapSet<>();

        MapSet<MainIdentifier, InteractorIdentifier> interactorMap = nodeSummary.getData().getInteractorMap();
        for (MainIdentifier diagramEntity : interactorMap.keySet()) {
            for (InteractorIdentifier interactor : interactorMap.getElements(diagramEntity)) {
                IdentifierSummary submitted = new IdentifierSummary(interactor.getId(), interactor.getExp());
                summaries.add(diagramEntity.getResource(), submitted);

                interactsWith.add(submitted, diagramEntity.getValue().getId());

                String interactorAcc = interactor.getMapsTo();
                mapsTo.add(submitted, interactorAcc);
            }
        }

        for (Resource resource : summaries.keySet()) {
            for (IdentifierSummary identifier : summaries.getElements(resource)) {
                identifiers.add(new FoundInteractor(identifier, mapsTo.getElements(identifier), new IdentifierMap(resource.getName(), interactsWith.getElements(identifier))));
            }
        }

        //IMPORTANT TO BE HERE!
        this.found = this.identifiers.size();
    }

    public List<FoundInteractor> getIdentifiers() {
        return identifiers;
    }

    public Set<String> getResources() {
        return resources;
    }

    public List<String> getExpNames() {
        return expNames;
    }

    public Integer getFound() {
        return found;
    }

    private List<FoundInteractor> filterByResource(String resource){
        List<FoundInteractor> rtn = new LinkedList<>();
        for (FoundInteractor interactor : identifiers) {
            IdentifierMap identifierMap = interactor.getInteractsWith();
            if(identifierMap.getResource().equals(resource)){
                rtn.add(interactor);
            }
        }
        return rtn;
    }

    public FoundInteractors filter(String resource){
        List<FoundInteractor> identifiers;
        if(resource.equals("TOTAL")){
            identifiers = this.identifiers;
        }else{
            identifiers = filterByResource(resource.toUpperCase());
        }
        return new FoundInteractors(identifiers, resources, expNames);
    }

    public FoundInteractors filter(String resource, Integer pageSize, Integer page) {
        resource = resource.toUpperCase();

        List<FoundInteractor> identifiers;
        if(resource.equals("TOTAL")){
            identifiers = this.identifiers;
        }else{
            identifiers = filterByResource(resource.toUpperCase());
        }

        pageSize = (pageSize==null) ? identifiers.size() : pageSize ;
        pageSize = pageSize < 0 ? 0 : pageSize;

        page = (page==null) ? 1 : page;
        page = page < 0 ? 0 : page;

        int from = pageSize * (page - 1);
        if(from < identifiers.size() && from > -1){
            int to = from + pageSize;
            to = to > identifiers.size() ? identifiers.size() : to;
            Set<String> resources = resource.equals("TOTAL") ? this.resources : new HashSet<String>(Arrays.asList(resource));
            return new FoundInteractors(identifiers.subList(from, to), resources, this.expNames);
        }else{
            return null;
        }
    }
}
