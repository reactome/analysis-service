package org.reactome.server.components.analysis.model;

import org.reactome.server.components.analysis.util.MapSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simple approach to take into account the number of reactions where a physical entity
 * have been seen while building the analysis structure
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class EntityPathwayReactionMap {

    private Map<Long, MapSet<Long, Long>> entityPathwayReactionMap;

    public EntityPathwayReactionMap() {
        this.entityPathwayReactionMap = new HashMap<Long, MapSet<Long, Long>>();
    }

    public void add(Long physicalEntityId, Long pathwayId, Long reactionId){
        MapSet<Long, Long> map = this.getOrCreatePathwayCounter(physicalEntityId);
        map.add(pathwayId, reactionId);
    }

    public void add(Long physicalEntity, Set<Long> pathwayIds, Long reactionId){
        for (Long pathwayId : pathwayIds) {
            this.add(physicalEntity, pathwayId, reactionId);
        }
    }

    public MapSet<Long, Long> getPathwaysReactions(Long physicalEntityId){
        return this.entityPathwayReactionMap.get(physicalEntityId);
    }

    public Set<Long> keySet(){
        return this.entityPathwayReactionMap.keySet();
    }

    private MapSet<Long, Long> getOrCreatePathwayCounter(Long physicalEntityId){
        MapSet<Long, Long> rtn = this.entityPathwayReactionMap.get(physicalEntityId);
        if(rtn==null){
            rtn = new MapSet<Long, Long>();
            this.entityPathwayReactionMap.put(physicalEntityId, rtn);
        }
        return rtn;
    }
}
