package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.identifier.Identifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.analysis.service.result.PathwayNodeSummary;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class FoundEntities {

    private List<FoundEntity> identifiers;
    private Set<String> resources;
    private List<String> expNames;
    private Integer found;

    private FoundEntities(List<FoundEntity> identifiers, Set<String> resources, List<String> expNames, Integer found) {
        this.identifiers = identifiers;
        this.resources = resources;
        this.expNames = expNames;
        this.found = found;
    }

    public FoundEntities(PathwayNodeSummary nodeSummary, List<String> expNames) {
        this.expNames = expNames;

        this.resources = new HashSet<String>();
        this.identifiers = new LinkedList<FoundEntity>();
        MapSet<Identifier, MainIdentifier> identifierMap = nodeSummary.getData().getIdentifierMap();
        for (Identifier identifier : identifierMap.keySet()) {

            MapSet<String, String> mapsTo = new MapSet<>();
            for (MainIdentifier mainIdentifier : identifierMap.getElements(identifier)) {
                String name = mainIdentifier.getResource().getName();
                this.resources.add(name);
                mapsTo.add(name, mainIdentifier.getValue().getId());
            }

            IdentifierSummary is = new IdentifierSummary(identifier.getValue());
            Set<IdentifierMap> maps = new HashSet<>();
            for (String resource : mapsTo.keySet()) {
                maps.add(new IdentifierMap(resource, mapsTo.getElements(resource)));
            }

            boolean added = false;
            for (FoundEntity foundEntity : this.identifiers) {
                if(foundEntity.getId().equals(is.getId())){
                    foundEntity.merge(maps);
                    added = true;
                    break;
                }
            }
            if(!added){
                this.identifiers.add(new FoundEntity(is, maps));
            }
        }
        //IMPORTANT TO BE HERE!
        this.found = this.identifiers.size();
    }

    public List<FoundEntity> getIdentifiers() {
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

    private List<FoundEntity> filterByResource(String resource){
        List<FoundEntity> rtn = new LinkedList<FoundEntity>();
        for (FoundEntity identifier : identifiers) {
            for (IdentifierMap identifierMap : identifier.getMapsTo()) {
                if(identifierMap.getResource().equals(resource)){
                    rtn.add(new FoundEntity(identifier, resource));
                }
            }
        }
        return rtn;
    }

    public FoundEntities filter(String resource){
        List<FoundEntity> identifiers;
        if(resource.equals("TOTAL")){
            identifiers = this.identifiers;
        }else{
            identifiers = filterByResource(resource.toUpperCase());
        }
        return new FoundEntities(identifiers, resources, expNames, identifiers.size());
    }

    public FoundEntities filter(String resource, Integer pageSize, Integer page) {
        resource = resource.toUpperCase();

        List<FoundEntity> identifiers;
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
            return new FoundEntities(identifiers.subList(from, to), resources, this.expNames, identifiers.size());
        }else{
            return null;
        }
    }
}
