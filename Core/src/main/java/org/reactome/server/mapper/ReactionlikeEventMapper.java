package org.reactome.server.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.reactome.server.controller.ReactomeToRESTfulAPIConverter;
import org.reactome.server.model.DatabaseObject;
import org.reactome.server.model.ReactionlikeEvent;

/**
 * This class is used to do some post-processing for ReactionlikeEvent.
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ReactionlikeEventMapper extends EventMapper {

	public ReactionlikeEventMapper(){
		
	}
	
	@Override
    public void fillDetailedView(GKInstance inst,
                                 DatabaseObject obj,
                                 ReactomeToRESTfulAPIConverter converter) throws Exception {
		super.fillDetailedView(inst, obj, converter);
		if (!validParameters(inst, obj))
			return;
		ReactionlikeEvent rle = (ReactionlikeEvent) obj;
		//Fetch the normal reactions
		fetchNormalReactions(inst, rle, converter);
	}
	
    private void fetchNormalReactions(GKInstance inst, 
    		ReactionlikeEvent currentReactionLikeEvent,
            ReactomeToRESTfulAPIConverter converter) throws Exception {
    	Collection<GKInstance> normalReactions = inst.getReferers(ReactomeJavaConstants.normalReaction);
        if (normalReactions != null && normalReactions.size() > 0) {
            List<ReactionlikeEvent> list = new ArrayList<ReactionlikeEvent>();
            for (GKInstance event : normalReactions) {
            	ReactionlikeEvent converted = (ReactionlikeEvent) converter.createObject(event);
                list.add(converted);
            }
            currentReactionLikeEvent.setNormalReaction(list);
        }
    }
    
    @Override
    protected boolean isValidObject(DatabaseObject obj) {
        return obj instanceof ReactionlikeEvent;
    }
}