package org.reactome.server.analysis.service.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayElements {

    private List<PathwayEntity> entities;
    private List<PathwayInteractor> interactors;
    private Set<String> resources;
    private List<String> expNames;
    private Integer foundEntities;
    private Integer foundInteractors;

    public PathwayElements(PathwayEntities entities, PathwayInteractors interactors, List<String> expNames) {
        this.resources = new HashSet<>();
        this.expNames = expNames;
        if(entities !=null){
            this.entities = entities.getIdentifiers();
            this.foundEntities = this.entities.size();
            this.resources.addAll(entities.getResources());
        }
        if(interactors!=null){
            this.interactors = interactors.getEntities();
            this.foundInteractors = this.interactors.size();
            this.resources.addAll(interactors.getResources());
        }
    }

    public List<PathwayEntity> getEntities() {
        return entities;
    }

    public List<PathwayInteractor> getInteractors() {
        return interactors;
    }

    public Set<String> getResources() {
        return resources;
    }

    public List<String> getExpNames() {
        return expNames;
    }

    public Integer getFoundEntities() {
        return foundEntities;
    }

    public Integer getFoundInteractors() {
        return foundInteractors;
    }
}
