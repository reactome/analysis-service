package org.reactome.server.analysis.tools.components.filter;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.core.controller.APIControllerHelper;
import org.reactome.core.model.Species;
import org.reactome.server.analysis.core.model.PathwayHierarchy;
import org.reactome.server.analysis.core.model.PathwayNode;
import org.reactome.server.analysis.core.model.SpeciesNode;
import org.reactome.server.analysis.core.model.SpeciesNodeFactory;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.analysis.tools.BuilderTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class PathwayHierarchyBuilder {
    @Autowired
    private APIControllerHelper helper;

    private Map<SpeciesNode, PathwayHierarchy> hierarchies = new HashMap<SpeciesNode, PathwayHierarchy>();
    private MapSet<Long, PathwayNode> pathwayLocation = new MapSet<Long, PathwayNode>();

    public void build(MySQLAdaptor dba) {
        //THIS IS NEEDED HERE
        this.helper.setDba(dba);

        List<Species> speciess = this.helper.getSpeciesList();
        int i = 0; int tot = speciess.size();
        for (Species species : speciess) {
//            if(!species.getDbId().equals(48887L)) break;
            if(BuilderTool.VERBOSE) {
                System.out.print("\rCreating the pathway hierarchies >> " + ++i + "/" + tot + " ");
            }
            SpeciesNode speciesNode = SpeciesNodeFactory.getSpeciesNode(species.getDbId(), species.getDisplayName());
            PathwayHierarchy pathwayHierarchy = new PathwayHierarchy(speciesNode);
            this.hierarchies.put(speciesNode, pathwayHierarchy);
            try{
                for (GKInstance instance : this.helper.getFrontPageItems(species.getDisplayName())) {
                    String stId =  this.getStableIdentifier(instance);
                    boolean hasDiagram = this.getHasDiagram(instance);
                    PathwayNode node = pathwayHierarchy.addFrontpageItem(stId, instance.getDBID(), instance.getDisplayName(), hasDiagram);
                    this.pathwayLocation.add(instance.getDBID(), node);
                    if(BuilderTool.VERBOSE) {
                        System.out.print("."); // Indicates progress
                    }
                    this.fillBranch(node, instance);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(BuilderTool.VERBOSE) {
            System.out.println("\rPathway hierarchies created successfully");
        }
    }

    private void fillBranch(PathwayNode node, GKInstance inst){
        try{
            if (inst.getSchemClass().isValidAttribute(ReactomeJavaConstants.hasEvent)) {
                List<?> children = inst.getAttributeValuesList(ReactomeJavaConstants.hasEvent);
                for (Object obj : children) {
                    GKInstance instance = (GKInstance) obj;
                    if(instance.getSchemClass().isa(ReactomeJavaConstants.Pathway)){
                        String stId = this.getStableIdentifier(instance);
                        boolean hasDiagram = this.getHasDiagram(instance);
                        PathwayNode aux = node.addChild(stId, instance.getDBID(), instance.getDisplayName(), hasDiagram);
                        this.pathwayLocation.add(instance.getDBID(), aux);
                        this.fillBranch(aux, instance);
                    }else{
                        node.setLowerLevelPathway(true); //if the pathways has other events than pathways means it is a lower leve pathway candidate
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Map<SpeciesNode, PathwayHierarchy> getHierarchies() {
        return hierarchies;
    }

    public MapSet<Long, PathwayNode> getPathwayLocation() {
        return pathwayLocation;
    }

    private String getStableIdentifier(GKInstance pathway){
        try {
            GKInstance stId = (GKInstance) pathway.getAttributeValue(ReactomeJavaConstants.stableIdentifier);
            return (String) stId.getAttributeValue(ReactomeJavaConstants.identifier);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean getHasDiagram(GKInstance pathway){
        try {
            Collection<?> diagrams = pathway.getReferers(ReactomeJavaConstants.representedPathway);
            if (diagrams != null && diagrams.size() > 0) {
                for (Object obj : diagrams) {
                    GKInstance diagram = (GKInstance) obj;
                    if (diagram.getSchemClass().isa(ReactomeJavaConstants.PathwayDiagram)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void prepareToSerialise(){
        if(BuilderTool.VERBOSE) {
            System.out.print("Setting up the resource counters for each Resource/PathwayNode");
        }
        for (SpeciesNode species : this.hierarchies.keySet()) {
            PathwayHierarchy hierarchy = this.hierarchies.get(species);
            hierarchy.setCountersAndCleanUp();
        }
        if(BuilderTool.VERBOSE) {
            System.out.println("\rResource counters set up successfully.");
        }
    }
}
