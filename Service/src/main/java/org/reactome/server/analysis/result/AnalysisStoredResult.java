package org.reactome.server.analysis.result;

import org.reactome.server.analysis.model.*;
import org.reactome.server.components.analysis.model.*;
import org.reactome.server.components.analysis.model.resource.MainResource;
import org.reactome.server.components.analysis.model.resource.Resource;
import org.reactome.server.components.analysis.model.resource.ResourceFactory;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class AnalysisStoredResult {
    private static final Integer PAGE_SIZE = 20;

    private AnalysisSummary summary;
    private Set<AnalysisIdentifier> notFound;
    private List<PathwayNodeSummary> pathways;
    private List<ResourceSummary> resourceSummary;
    private ExpressionSummary expressionSummary;

    public AnalysisStoredResult(UserData userData, HierarchiesData data){
        this.notFound = data.getNotFound();
        this.pathways = new LinkedList<PathwayNodeSummary>();
        this.expressionSummary = new ExpressionSummary(userData);
    }

    public void setHitPathways(List<PathwayNode> pathwayNodes){
        //At the time we set the hit pathways, we also initialize resource summary
        Map<String, Integer> aux = new HashMap<String, Integer>();
        Integer total = 0;
        for (PathwayNode pathwayNode : pathwayNodes) {
            total++;
            for (MainResource mainResource : pathwayNode.getPathwayNodeData().getResources()) {
                if(pathwayNode.getPathwayNodeData().getEntitiesFound(mainResource)>0){
                    Integer n = aux.get(mainResource.getName());
                    aux.put(mainResource.getName(), n == null ? 1 : n + 1 );
                }
            }
            this.pathways.add(new PathwayNodeSummary(pathwayNode));
        }
        resourceSummary = new LinkedList<ResourceSummary>();
        for (String resource : aux.keySet()) {
            resourceSummary.add(new ResourceSummary(resource, aux.get(resource)));
        }
        Collections.sort(resourceSummary, Collections.reverseOrder());
        //Total is always inserted on top
        resourceSummary.add(0, new ResourceSummary("TOTAL", total));
    }

    public void setSummary(AnalysisSummary summary) {
        this.summary = summary;
    }

    public ExpressionSummary getExpressionSummary() {
        return expressionSummary;
    }

    public AnalysisSummary getSummary() {
        return summary;
    }

    public Set<AnalysisIdentifier> getNotFound() {
        return notFound;
    }

    public PathwayNodeSummary getPathway(Long dbId){
        for (PathwayNodeSummary nodeSummary : this.pathways) {
            if(nodeSummary.getPathwayId().equals(dbId)){
                return nodeSummary;
            }
        }
        return null;
    }

    public List<PathwayNodeSummary> getPathways() {
        return pathways;
    }

    public List<ResourceSummary> getResourceSummary() {
        return resourceSummary;
    }

    public int getPage(Long dbId, String sortBy, String order, String resource, Integer pageSize){
        this.filterPathwaysByResource(resource);
        Collections.sort(this.pathways, getComparator(sortBy, order, resource));
        if(pageSize==null) pageSize = PAGE_SIZE;
        for (int i = 0; i < this.pathways.size(); i++) {
            PathwayNodeSummary pathway = this.pathways.get(i);
            if(pathway.getPathwayId().equals(dbId)){
                return ((int) Math.floor(i/pageSize)) + 1;
            }
        }
        return -1;
    }

    public AnalysisResult getResultSummary(String sortBy, String order, String resource, Integer pageSize, Integer page){
        this.filterPathwaysByResource(resource);
        Collections.sort(this.pathways, getComparator(sortBy, order, resource));
        if(pageSize==null) pageSize = PAGE_SIZE;
        List<PathwaySummary> rtn = new LinkedList<PathwaySummary>();
        if(page!=null && page>0){ // && this.pathways.size()>(pageSize*(page-1))){
            int end = (pageSize * page) > this.pathways.size()? this.pathways.size() : (pageSize * page);
            for(int i = pageSize*(page-1); i<end; ++i){
                PathwayNodeSummary pathwayNodeSummary = this.pathways.get(i);
                rtn.add(new PathwaySummary(pathwayNodeSummary, resource.toUpperCase()));
            }
        }else{
            for (PathwayNodeSummary pathway : this.pathways) {
                rtn.add(new PathwaySummary(pathway, resource.toUpperCase()));
            }
        }
        return new AnalysisResult(this, rtn);
    }

    public List<PathwaySummary> filterByPathways(List<Long> pathwayIds, String resource){
        this.filterPathwaysByResource(resource);
        List<PathwaySummary> rtn = new LinkedList<PathwaySummary>();
        for (PathwayNodeSummary pathway : this.pathways) {
            if(pathwayIds.contains(pathway.getPathwayId())){
                rtn.add(new PathwaySummary(pathway, resource.toUpperCase()));
            }
        }
        return rtn;
    }

    private Comparator<PathwayNodeSummary> getComparator(String sortBy, String order, String resource){
        AnalysisSortType sortType = AnalysisSortType.getSortType(sortBy);
        if(resource!=null){
            Resource r = ResourceFactory.getResource(resource);
            if(r!=null && r instanceof MainResource){
                MainResource mr = (MainResource) r;
                if(order!=null && order.toUpperCase().equals("DESC")){
                    return Collections.reverseOrder(ComparatorFactory.getComparator(sortType, mr));
                }else{
                    return ComparatorFactory.getComparator(sortType, mr);
                }
            }
        }
        if(order!=null && order.toUpperCase().equals("DESC")){
            return Collections.reverseOrder(ComparatorFactory.getComparator(sortType));
        }else{
            return ComparatorFactory.getComparator(sortType);
        }
    }

    private void filterPathwaysByResource(String resource){
        if(!resource.toUpperCase().equals("TOTAL")){
            List<PathwayNodeSummary> rtn = new LinkedList<PathwayNodeSummary>();
            Resource r = ResourceFactory.getResource(resource);
            if(r instanceof MainResource){
                MainResource mr = (MainResource) r;
                for (PathwayNodeSummary pathway : this.pathways) {
                    if(pathway.getData().getEntitiesFound(mr)>0){
                        rtn.add(pathway);
                    }
                }
            }
            this.pathways = rtn;
        }
    }
}
