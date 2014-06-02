/*
 * Created on Jun 5, 2012
 *
 */
package org.reactome.server.mapper;

import java.util.Collection;
import java.util.Iterator;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.reactome.server.controller.ReactomeToRESTfulAPIConverter;
import org.reactome.server.model.DatabaseObject;
import org.reactome.server.model.Pathway;

/**
 * @author gwu
 *
 */
public class PathwayMapper extends EventMapper {
    
    public PathwayMapper() {
        
    }

    @Override
    public void postProcess(GKInstance inst, 
                            DatabaseObject obj,
                            ReactomeToRESTfulAPIConverter converter) throws Exception {
        super.postProcess(inst, obj, converter);
        if (!validParameters(inst, obj))
            return;
        addPathwayDiagramFlag(inst, obj);
    }

    private void addPathwayDiagramFlag(GKInstance inst, DatabaseObject obj) throws Exception {
        // Check if this Pathway has Diagram
        Pathway pathway = (Pathway) obj;
        pathway.setHasDiagram(false);
        Collection<?> diagrams = inst.getReferers(ReactomeJavaConstants.representedPathway);
        if (diagrams != null && diagrams.size() > 0) {
            for (Iterator<?> it = diagrams.iterator(); it.hasNext();) {
                GKInstance diagram = (GKInstance) it.next();
                if (diagram.getSchemClass().isa(ReactomeJavaConstants.PathwayDiagram)) {
                    pathway.setHasDiagram(true);
                    break;
                }
            }
        }
    }

    @Override
    public void postShellProcess(GKInstance inst, DatabaseObject obj)
            throws Exception {
        super.postShellProcess(inst, obj);
        addPathwayDiagramFlag(inst, obj);
    }

}
