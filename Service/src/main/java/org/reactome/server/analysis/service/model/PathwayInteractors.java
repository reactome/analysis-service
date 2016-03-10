package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.identifier.Identifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.analysis.service.result.PathwayNodeSummary;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayInteractors {

    private List<PathwayInteractor> entities;
    private Set<String> resources;
    private List<String> expNames;
    private Integer found;

    private PathwayInteractors(List<PathwayInteractor> entities, Set<String> resources, List<String> expNames, Integer found) {
        this.entities = entities;
        this.resources = resources;
        this.expNames = expNames;
        this.found = found;
    }

    public PathwayInteractors(PathwayNodeSummary nodeSummary, List<String> expNames) {
        this.expNames = expNames;

        this.resources = new HashSet<String>();
        this.entities = new ArrayList<>();
        MapSet<MainIdentifier, Identifier> interactorMap = nodeSummary.getData().getInteractorMap();
        if(interactorMap!=null && !interactorMap.keySet().isEmpty()) {
            for (MainIdentifier mainIdentifier : interactorMap.keySet()) {
                this.resources.add(mainIdentifier.getResource().getName());
                this.entities.add(new PathwayInteractor(mainIdentifier, interactorMap.getElements(mainIdentifier)));
            }
        }
        //IMPORTANT TO BE HERE!
        this.found = this.entities.size();
    }

    public List<PathwayInteractor> getEntities() {
        return entities;
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

    private List<PathwayInteractor> filterByResource(String resource){
        List<PathwayInteractor> rtn = new LinkedList<>();
        for (PathwayInteractor interactor : entities) {
            if(interactor.getResource().equals(resource)){
                rtn.add(interactor);
            }
        }
        return rtn;
    }

    public PathwayInteractors filter(String resource) {
        List<PathwayInteractor> interactors;
        if(resource.equals("TOTAL")){
            interactors = this.entities;
        }else{
            interactors = filterByResource(resource.toUpperCase());
        }
        return new PathwayInteractors(interactors, resources, expNames, interactors.size());
    }

    public PathwayInteractors filter(String resource, Integer pageSize, Integer page) {
        resource = resource.toUpperCase();

        List<PathwayInteractor> interactors;
        if(resource.equals("TOTAL")){
            interactors = this.entities;
        }else{
            interactors = filterByResource(resource.toUpperCase());
        }

        pageSize = (pageSize==null) ? interactors.size() : pageSize ;
        pageSize = pageSize < 0 ? 0 : pageSize;

        page = (page==null) ? 1 : page;
        page = page < 0 ? 0 : page;

        int from = pageSize * (page - 1);
        if(from < interactors.size() && from > -1){
            int to = from + pageSize;
            to = to > interactors.size() ? interactors.size() : to;
            Set<String> resources = resource.equals("TOTAL") ? this.resources : new HashSet<String>(Arrays.asList(resource));
            return new PathwayInteractors(interactors.subList(from, to), resources, this.expNames, interactors.size());
        }else{
            return null;
        }
    }
}
