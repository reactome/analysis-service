package org.reactome.server.analysis.core.model;

import org.reactome.server.analysis.core.model.identifier.Identifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.analysis.core.util.MathUtilities;

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

        Integer totalInteractors = 0; //Pre-calculated in setCounters method
        Integer foundInteractors = 0;
        Double interactorsRatio;

        //TP-Based analysis
        Integer totalReactions = 0; //Pre-calculated in setCounters method
        Integer foundReactions = 0;
        Double reactionsRatio;
    }

    /*
    The following structure plays two different roles
    1) While building the data structure it will keep track of the contained physical entities in the pathway
    2) During the analysis it will keep track of the seen elements
    */
    private MapSet<Identifier, MainIdentifier> map = new MapSet<>();

    /*
    The following structure plays two different roles
    1) While building the data structure it will keep track of the contained reactions in the pathway
    2) During the analysis it will keep track of the seen reactions
    */
    private MapSet<MainResource, AnalysisReaction> reactions = new MapSet<>();

    /*
    The following structure plays two different roles
    1) While building the data structure it will keep track of the contained physical entities in the pathway
    2) During the analysis it will keep track of the seen elements
    */
    private MapSet<MainIdentifier, Identifier> interactors = new MapSet<>();

    //Analysis result containers
    private Map<MainResource, Counter> entitiesResult = new HashMap<>();
    private Counter combinedResult = new Counter();  //All main identifiers combined in one result

    public PathwayNodeData() {
    }

    public void addMapping(Identifier identifier, MainIdentifier mainIdentifier) {
        this.map.add(identifier, mainIdentifier);
    }

    public void addReactions(MainResource mainResource, Set<AnalysisReaction> reactions) {
        this.reactions.add(mainResource, reactions);
    }

    public void addInteractors(MainIdentifier mainIdentifier, Identifier identifier) {
        this.interactors.add(mainIdentifier, identifier);
    }


    // ENTITIES Result

    public Set<AnalysisIdentifier> getEntities() {
        Set<AnalysisIdentifier> rtn = new HashSet<>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : this.map.getElements(identifier)) {
                rtn.add(mainIdentifier.getValue());
            }
        }
        return rtn;
    }

    public Set<AnalysisIdentifier> getEntities(MainResource resource) {
        Set<AnalysisIdentifier> rtn = new HashSet<>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : this.map.getElements(identifier)) {
                if (mainIdentifier.getResource().equals(resource)) {
                    rtn.add(mainIdentifier.getValue());
                }
            }
        }
        return rtn;
    }

    private List<List<Double>> groupEntitiesExpressionValues(Collection<AnalysisIdentifier> identifiers) {
        List<List<Double>> evss = new LinkedList<>();
        for (AnalysisIdentifier identifier : identifiers) {
            int i = 0;
            for (Double ev : identifier.getExp()) {
                List<Double> evs;
                if (evss.size() > i) {
                    evs = evss.get(i);
                } else {
                    evs = new LinkedList<Double>();
                    evss.add(i, evs);
                }
                evs.add(ev);
                i++;
            }
        }
        return evss;
    }

    private List<Double> calculateAverage(List<List<Double>> expressionValues) {
        List<Double> avg = new LinkedList<>();
        int i = 0;
        for (List<Double> evs : expressionValues) {
            Double sum = 0.0;
            Double total = 0.0;
            for (Double ev : evs) {
                if (ev != null) {
                    sum += ev;
                    total++;
                }
            }
            if (total > 0.0) {
                avg.add(i++, sum / total);
            } else {
                avg.add(i++, null);
            }
        }
        return avg;
    }

    private List<AnalysisIdentifier> getEntitiesDuplication() {
        List<AnalysisIdentifier> rtn = new LinkedList<>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : map.getElements(identifier)) {
                rtn.add(mainIdentifier.getValue());
            }
        }
        return rtn;
    }

    private List<AnalysisIdentifier> getEntitiesDuplication(MainResource resource) {
        List<AnalysisIdentifier> rtn = new LinkedList<>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : map.getElements(identifier)) {
                if (mainIdentifier.getResource().equals(resource)) {
                    rtn.add(mainIdentifier.getValue());
                }
            }
        }
        return rtn;
    }

    public List<Double> getExpressionValuesAvg() {
        return calculateAverage(groupEntitiesExpressionValues(getEntitiesDuplication()));
    }

    public List<Double> getExpressionValuesAvg(MainResource resource) {
        return calculateAverage(groupEntitiesExpressionValues(getEntitiesDuplication(resource)));
    }

    public Integer getEntitiesCount() {
        return this.combinedResult.totalEntities;
    }

    public Integer getEntitiesCount(MainResource resource) {
        Counter counter = this.entitiesResult.get(resource);
        if (counter != null) {
            return counter.totalEntities;
        }
        return 0;
    }

    public Integer getEntitiesFound() {
        return getEntities().size();
    }

    public Integer getEntitiesFound(MainResource resource) {
        return getEntities(resource).size();
    }

    public Double getEntitiesPValue() {
        return this.combinedResult.entitiesPValue;
    }

    public Double getEntitiesPValue(MainResource resource) {
        Counter counter = this.entitiesResult.get(resource);
        if (counter != null) {
            return counter.entitiesPValue;
        }
        return null;
    }

    public Double getEntitiesFDR() {
        return this.combinedResult.entitiesFDR;
    }

    public Double getEntitiesFDR(MainResource resource) {
        Counter counter = this.entitiesResult.get(resource);
        if (counter != null) {
            return counter.entitiesFDR;
        }
        return null;
    }

    public Double getEntitiesRatio() {
        return this.combinedResult.entitiesRatio;
    }

    public Double getEntitiesRatio(MainResource resource) {
        Counter counter = this.entitiesResult.get(resource);
        if (counter != null) {
            return counter.entitiesRatio;
        }
        return null;
    }

    public Double getInteractorsRatio() {
        return this.combinedResult.interactorsRatio;
    }

    public Double getInteractorsRatio(MainResource resource) {
        Counter counter = this.entitiesResult.get(resource);
        if (counter != null) {
            return counter.interactorsRatio;
        }
        return null;
    }

    //TODO: Provide with the pathway identifiers mapping to main identifiers
    public MapSet<Identifier, MainIdentifier> getIdentifierMap() {
        return map;
    }


    public MapSet<MainIdentifier, Identifier> getInteractorMap() {
        return interactors;
    }

    // INTERACTORS Result

    public Set<Identifier> getInteractors(){
        return interactors.values();
    }

    public Set<AnalysisIdentifier> getInteractors(MainResource resource){
        Set<AnalysisIdentifier> rtn = new HashSet<>();
        for (MainIdentifier mainIdentifier : interactors.keySet()) {
            for (Identifier identifier : interactors.getElements(mainIdentifier)) {
                if(identifier.getResource().equals(resource)){
                    rtn.add(mainIdentifier.getValue());
                }
            }
        }
        return rtn;
    }

    public Integer getInteractorsCount(){
        return this.combinedResult.totalInteractors;
    }

    public Integer getInteractorsCount(MainResource resource){
        Counter counter = this.entitiesResult.get(resource);
        if (counter != null) {
            return counter.totalInteractors;
        }
        return 0;
    }

    public Integer getInteractorsFound(){
        return getInteractors().size();
    }

    public Integer getInteractorsFound(MainResource resource){
        return getInteractors(resource).size();
    }


    // REACTIONS Result

    public Set<AnalysisReaction> getReactions() {
        return reactions.values();
    }

    public Set<AnalysisReaction> getReactions(MainResource resource) {
        Set<AnalysisReaction> rtn = reactions.getElements(resource);
        if(rtn==null){
            rtn = new HashSet<AnalysisReaction>();
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

    public Integer getInteractorsReactionsCount() {
        return this.combinedResult.totalReactions;
    }

    public Integer getInteractorsReactionsCount(MainResource mainResource) {
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
        return !map.isEmpty() || !interactors.isEmpty();
    }

    public void setEntitiesFDR(Double fdr){
        this.combinedResult.entitiesFDR = fdr;
    }

    public void setEntitiesFDR(MainResource resource, Double fdr){
        this.entitiesResult.get(resource).entitiesFDR = fdr;
    }

    //This is only called in build time
    protected void setCounters(PathwayNodeData speciesData){
        Set<AnalysisReaction> totalReactions = new HashSet<>();
        for (MainResource mainResource : reactions.keySet()) {
            Counter counter = getOrCreateCounter(mainResource);
            counter.totalReactions = reactions.getElements(mainResource).size();
            totalReactions.addAll(reactions.getElements(mainResource));
            counter.reactionsRatio = counter.totalReactions/speciesData.getReactionsCount(mainResource).doubleValue();
        }
        combinedResult.totalReactions += totalReactions.size(); totalReactions.clear();
        combinedResult.reactionsRatio =  combinedResult.totalReactions /speciesData.getReactionsCount().doubleValue();
        reactions = new MapSet<MainResource, AnalysisReaction>();

        MapSet<MainResource, AnalysisIdentifier> aux = new MapSet<>();
        for (Identifier identifier : map.keySet()) {
            for (MainIdentifier mainIdentifier : map.getElements(identifier)) {
                aux.add(mainIdentifier.getResource(), mainIdentifier.getValue());
            }
        }
        for (MainResource mainResource : aux.keySet()) {
            Counter counter = this.getOrCreateCounter(mainResource);
            counter.totalEntities = aux.getElements(mainResource).size();
            counter.entitiesRatio =  counter.totalEntities/speciesData.getEntitiesCount(mainResource).doubleValue();
            combinedResult.totalEntities += counter.totalEntities;
        }
        combinedResult.entitiesRatio = this.combinedResult.totalEntities / speciesData.getEntitiesCount().doubleValue();
        map = new MapSet<>();

        //INTERACTORS
        MapSet<MainResource, AnalysisIdentifier> temp = new MapSet<>();
        //To ensure the main resource is present, we take into account the resource of the molecule PRESENT in the diagram
        for (MainIdentifier mainIdentifier : interactors.keySet()) {
            for (Identifier identifier : interactors.getElements(mainIdentifier)) {
                temp.add(mainIdentifier.getResource(), identifier.getValue());
            }
        }
        for (MainResource mainResource : aux.keySet()) {
            Counter counter = this.getOrCreateCounter(mainResource);
            Set<AnalysisIdentifier> interactors = temp.getElements(mainResource);
            counter.totalInteractors = interactors == null ? 0 : interactors.size();
            counter.interactorsRatio = (counter.totalEntities + counter.totalInteractors) / (double) (speciesData.getEntitiesCount(mainResource) + speciesData.getInteractorsCount(mainResource)) ;
            combinedResult.totalInteractors += counter.totalInteractors;
        }
        combinedResult.interactorsRatio = (combinedResult.totalEntities + combinedResult.totalInteractors) / (double) (speciesData.getEntitiesCount() + speciesData.getInteractorsCount());
        interactors = new MapSet<>();
    }

    public void setResultStatistics(Map<MainResource, Integer> sampleSizePerResource, Integer notFound, boolean includeInteractors){
        for (MainResource mainResource : this.getResources()) {
            Counter counter = this.entitiesResult.get(mainResource);
            counter.foundEntities = getEntitiesFound(mainResource);
            counter.foundReactions = getReactionsFound(mainResource);

            int found;
            if (includeInteractors) {
                counter.foundInteractors = getInteractorsFound(mainResource);
                found = counter.foundEntities + counter.foundInteractors;
            } else {
                found = counter.foundEntities;
            }
            if (found > 0) {
                Integer sampleSize = sampleSizePerResource.get(mainResource) + notFound;
                double ratio = includeInteractors ? counter.interactorsRatio : counter.entitiesRatio;
                counter.entitiesPValue = MathUtilities.calculatePValue(ratio, sampleSize, found);
            }
        }

        Counter counter = combinedResult;
        counter.foundEntities = getEntitiesFound();
        int found;
        if(includeInteractors) {
            counter.foundInteractors = getInteractorsFound();
            found = counter.foundEntities + counter.foundInteractors;
        } else {
            found = counter.foundEntities;
        }
        if( found > 0 ){
            Integer sampleSize = notFound;
            for (MainResource mainResource : sampleSizePerResource.keySet()) {
                sampleSize += sampleSizePerResource.get(mainResource);
            }
            double ratio = includeInteractors ? counter.interactorsRatio : counter.entitiesRatio;
            counter.entitiesPValue = MathUtilities.calculatePValue(ratio, sampleSize, counter.foundEntities);
        }
        counter.foundReactions = getReactionsFound();
    }

    protected Double getScore(){
        return getScore(this.combinedResult);
    }

    protected Double getScore(MainResource mainResource){
        return getScore(this.entitiesResult.get(mainResource));
    }

    private Double getScore(Counter counter){
        Double entitiesPercentage = counter.foundEntities / counter.totalEntities.doubleValue();
        Double reactionsPercentage = counter.foundReactions / counter.totalReactions.doubleValue();
        return (0.75 * (reactionsPercentage)) + (0.25 * (entitiesPercentage));
    }

    private Counter getOrCreateCounter(MainResource mainResource){
        Counter counter = this.entitiesResult.get(mainResource);
        if (counter == null) {
            counter = new Counter();
            this.entitiesResult.put(mainResource, counter);
        }
        return counter;
    }
}