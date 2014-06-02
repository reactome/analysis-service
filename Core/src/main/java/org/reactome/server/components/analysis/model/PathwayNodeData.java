package org.reactome.server.components.analysis.model;

import org.reactome.server.components.analysis.model.identifier.Identifier;
import org.reactome.server.components.analysis.model.identifier.MainIdentifier;
import org.reactome.server.components.analysis.model.resource.MainResource;
import org.reactome.server.components.analysis.util.MapSet;
import org.reactome.server.components.analysis.util.MathUtilities;

import java.util.*;

/**
 * For each pathway, this class contains the result of the analysis. There are three main parts in
 * this class: (1) the mapping between identifiers provided by the user and main identifiers used
 * for the result, (2) The set of reactions identifiers found for each main resource and (3) the
 * result counters -and statistics- for each main resource and for the combination of all of them
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class PathwayNodeData {
    //Please note that counter is used for each main identifier and for the combinedResult
    class Counter {
        Integer totalEntities = 0; //Pre-calculated in setCounters method
        Integer foundEntities = 0;
        Double entitiesRatio;
        Double entitiesPValue;
        Double entitiesFDR;

        //TP-Based analysis (this is in beta)
        Integer totalReactions = 0; //Pre-calculated in setCounters method
        Integer foundReactions = 0;
        Double reactionsRatio;
        Double reactionsPValue;
        Double reactionsFDR;
    }

    /*
    The following structure plays two different roles
    1) While building the data structure it will keep track of the contained physical entities in the pathway
    2) During the analysis it will keep track of the seen elements
    */
    private MapSet<Identifier, MainIdentifier> map;

    /*
    The following structure plays two different roles
    1) While building the data structure it will keep track of the contained reactions in the pathway
    2) During the analysis it will keep track of the seen reactions
    */
    private MapSet<MainResource, Long> reactions;

    //Analysis result containers
    private Map<MainResource, Counter> entitiesResult;
    private Counter combinedResult;  //All main identifiers combined in one result

    public PathwayNodeData() {
        this.map = new MapSet<Identifier, MainIdentifier>();
        this.reactions = new MapSet<MainResource, Long>();
        this.entitiesResult = new HashMap<MainResource, Counter>();
        this.combinedResult = new Counter();
    }

    public void addMapping(Identifier identifier, MainIdentifier mainIdentifier){
        this.map.add(identifier, mainIdentifier);
    }

    public void addReactions(MainResource mainResource, Set<Long> reactions){
        this.reactions.add(mainResource, reactions);
    }

    // ENTITIES Result

    public Set<AnalysisIdentifier> getEntities(){
        Set<AnalysisIdentifier> rtn = new HashSet<AnalysisIdentifier>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : this.map.getElements(identifier)) {
                rtn.add(mainIdentifier.getValue());
            }
        }
        return rtn;
    }

    public Set<AnalysisIdentifier> getEntities(MainResource resource){
        Set<AnalysisIdentifier> rtn = new HashSet<AnalysisIdentifier>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : this.map.getElements(identifier)) {
                if(mainIdentifier.getResource().equals(resource)){
                    rtn.add(mainIdentifier.getValue());
                }
            }
        }
        return rtn;
    }

    private List<List<Double>> groupEntitiesExpressionValues(Collection<AnalysisIdentifier> identifiers){
        List<List<Double>> evss = new LinkedList<List<Double>>();
        for (AnalysisIdentifier identifier : identifiers) {
            int i = 0;
            for (Double ev : identifier.getExp()) {
                List<Double> evs;
                if(evss.size()>i){
                    evs = evss.get(i);
                }else{
                    evs = new LinkedList<Double>();
                    evss.add(i, evs);
                }
                evs.add(ev);
                i++;
            }
        }
        return evss;
    }

    private List<Double> calculateAverage(List<List<Double>> expressionValues){
        List<Double> avg = new LinkedList<Double>();
        int i = 0;
        for (List<Double> evs : expressionValues) {
            Double sum = 0.0; Double total = 0.0;
            for (Double ev : evs) {
                if(ev!=null){
                    sum += ev;
                    total++;
                }
            }
            avg.add(i++, sum / total);
        }
        return avg;
    }

    private List<AnalysisIdentifier> getEntitiesDuplication(){
        List<AnalysisIdentifier> rtn = new LinkedList<AnalysisIdentifier>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : map.getElements(identifier)) {
                rtn.add(mainIdentifier.getValue());
            }
        }
        return rtn;
    }

    private List<AnalysisIdentifier> getEntitiesDuplication(MainResource resource){
        List<AnalysisIdentifier> rtn = new LinkedList<AnalysisIdentifier>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : map.getElements(identifier)) {
                if(mainIdentifier.getResource().equals(resource)){
                    rtn.add(mainIdentifier.getValue());
                }
            }
        }
        return rtn;
    }

    public List<Double> getExpressionValuesAvg(){
        return calculateAverage(groupEntitiesExpressionValues(getEntitiesDuplication()));
    }

    public List<Double> getExpressionValuesAvg(MainResource resource){
        return calculateAverage(groupEntitiesExpressionValues(getEntitiesDuplication(resource)));
    }

    public Integer getEntitiesCount(){
        return this.combinedResult.totalEntities;
    }

    public Integer getEntitiesCount(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if(counter!=null){
            return counter.totalEntities;
        }
        return 0;
    }

    public Integer getEntitiesFound(){
        return this.getEntities().size();
    }

    public Integer getEntitiesFound(MainResource resource){
        return this.getEntities(resource).size();
    }

    public Double getEntitiesPValue(){
        return this.combinedResult.entitiesPValue;
    }

    public Double getEntitiesPValue(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if(counter!=null){
            return counter.entitiesPValue;
        }
        return null;
    }

    public Double getEntitiesFDR(){
        return this.combinedResult.entitiesFDR;
    }

    public Double getEntitiesFDR(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if(counter!=null){
            return counter.entitiesFDR;
        }
        return null;
    }

    public Double getEntitiesRatio(){
        return this.combinedResult.entitiesRatio;
    }

    public Double getEntitiesRatio(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if(counter!=null){
            return counter.entitiesRatio;
        }
        return null;
    }

    //TODO: Provide with the pathway identifiers mapping to main identifiers
    public MapSet<Identifier, MainIdentifier> getIdentifierMap() {
        return map;
    }

    // REACTIONS Result

    public Set<Long> getReactions() {
        Set<Long> rtn = new HashSet<Long>();
        for (MainResource resource : reactions.keySet()) {
            rtn.addAll(reactions.getElements(resource));
        }
        return rtn;
    }

    public Set<Long> getReactions(MainResource resource) {
        Set<Long> rtn = reactions.getElements(resource);
        if(rtn==null){
            rtn = new HashSet<Long>();
        }
        return rtn;
    }

    public Integer getReactionsCount() {
        return this.combinedResult.totalReactions;
    }

    public Integer getReactionsCount(MainResource mainResource) {
        Counter counter = this.entitiesResult.get(mainResource);
        if(counter!=null){
            return counter.totalReactions;
        }
        return 0;
    }

    public Integer getReactionsFound(){
        return this.getReactions().size();
    }

    public Integer getReactionsFound(MainResource resource){
        return this.getReactions(resource).size();
    }

    public Double getReactionsPValue(){
        return this.combinedResult.reactionsPValue;
    }

    public Double getReactionsPValue(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if(counter!=null){
            return counter.reactionsPValue;
        }
        return null;
    }

    public Double getReactionsFDR(){
        return this.combinedResult.reactionsFDR;
    }

    public Double getReactionsFDR(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if(counter!=null){
            return counter.reactionsFDR;
        }
        return null;
    }

    public Double getReactionsRatio(){
        return this.combinedResult.reactionsRatio;
    }

    public Double getReactionsRatio(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if(counter !=null){
            return counter.reactionsRatio;
        }
        return null;
    }

    public Set<MainResource> getResources(){
        return this.entitiesResult.keySet();
    }

    public boolean hasResult(){
        for (MainResource resource : this.entitiesResult.keySet()) {
            Counter counter = this.entitiesResult.get(resource);
            if(counter != null && counter.entitiesRatio != null){
                return true;
            }
        }
        return false;
    }

    public void setEntitiesFDR(Double fdr){
        this.combinedResult.entitiesFDR = fdr;
    }

    public void setEntitiesFDR(MainResource resource, Double fdr){
        this.entitiesResult.get(resource).entitiesFDR = fdr;
    }

    public void setReactionsFDR(Double fdr){
        this.combinedResult.reactionsFDR = fdr;
    }

    public void setReactionsFDR(MainResource resource, Double fdr){
        this.entitiesResult.get(resource).reactionsFDR = fdr;
    }

    //This is only called in build time
    protected void setCounters(){
        Set<Long> totalReactions = new HashSet<Long>();
        for (MainResource mainResource : this.reactions.keySet()) {
            Counter counter = this.getOrCreateCounter(mainResource);
            counter.totalReactions = this.reactions.getElements(mainResource).size();
            totalReactions.addAll(this.reactions.getElements(mainResource));
        }
        this.combinedResult.totalReactions += totalReactions.size(); totalReactions.clear();
        this.reactions = new MapSet<MainResource, Long>();

        MapSet<MainResource, AnalysisIdentifier> aux = new MapSet<MainResource, AnalysisIdentifier>();
        for (Identifier identifier : this.map.keySet()) {
            for (MainIdentifier mainIdentifier : this.map.getElements(identifier)) {
                aux.add(mainIdentifier.getResource(), mainIdentifier.getValue());
            }
        }
        for (MainResource mainResource : aux.keySet()) {
            Counter counter = this.getOrCreateCounter(mainResource);
            counter.totalEntities = aux.getElements(mainResource).size();
            this.combinedResult.totalEntities += counter.totalEntities;
        }
        this.map = new MapSet<Identifier, MainIdentifier>();
    }

    public void setResultStatistics(PathwayNodeData speciesData, Map<MainResource, Integer> sampleSizePerResource, Integer notFound){
        for (MainResource mainResource : this.getResources()) {
            Counter counter = this.entitiesResult.get(mainResource);
            counter.foundEntities = this.getEntitiesFound(mainResource);
            if( counter.foundEntities > 0 ){
                Integer sampleSize = sampleSizePerResource.get(mainResource) + notFound;
                counter.entitiesRatio =  counter.totalEntities/speciesData.getEntitiesCount(mainResource).doubleValue();
                counter.entitiesPValue =  MathUtilities.calculatePValue(counter.entitiesRatio, sampleSize, counter.foundEntities);
            }

            counter.foundReactions = this.getReactionsFound(mainResource);
            if( counter.foundReactions > 0 ){
                Integer reactionsSize = speciesData.getReactionsFound(mainResource);
                counter.reactionsRatio = counter.totalReactions/speciesData.getReactionsCount(mainResource).doubleValue();
                counter.reactionsPValue = MathUtilities.calculatePValue(counter.reactionsRatio, reactionsSize, counter.foundReactions);
            }
        }

        Counter counter = this.combinedResult;
        counter.foundEntities = this.getEntitiesFound();
        if( counter.foundEntities > 0 ){
            Integer sampleSize = notFound;
            for (MainResource mainResource : sampleSizePerResource.keySet()) {
                sampleSize += sampleSizePerResource.get(mainResource);
            }
            counter.entitiesRatio = counter.totalEntities/speciesData.getEntitiesCount().doubleValue();
            counter.entitiesPValue = MathUtilities.calculatePValue(counter.entitiesRatio, sampleSize, counter.foundEntities);
        }
        counter.foundReactions = this.getReactionsFound();
        if( counter.foundReactions > 0) {
            Integer reactionsSize = speciesData.getReactionsFound();
            counter.reactionsRatio = counter.totalReactions/speciesData.getReactionsCount().doubleValue();
            counter.reactionsPValue = MathUtilities.calculatePValue(counter.reactionsRatio, reactionsSize, counter.foundReactions);
        }
    }

    private Counter getOrCreateCounter(MainResource mainResource){
        Counter counter = this.entitiesResult.get(mainResource);
        if(counter ==null){
            counter = new Counter();
            this.entitiesResult.put(mainResource, counter);
        }
        return counter;
    }
}