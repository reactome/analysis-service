package org.reactome.server.components.exporter;

import org.gk.persistence.MySQLAdaptor;
import org.reactome.server.components.analysis.data.AnalysisData;
import org.reactome.server.components.analysis.data.HierarchiesDataContainer;
import org.reactome.server.components.analysis.model.AnalysisIdentifier;
import org.reactome.server.components.analysis.model.PathwayNode;
import org.reactome.server.components.analysis.model.PhysicalEntityNode;
import org.reactome.server.components.analysis.model.identifier.MainIdentifier;
import org.reactome.server.components.analysis.model.resource.ResourceFactory;
import org.reactome.server.components.analysis.util.MapSet;
import org.reactome.server.controller.APIControllerHelper;
import org.reactome.server.model.Pathway;
import org.reactome.server.model.Species;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class Exporter {
    @Autowired
    private AnalysisData analysisData;

    @Autowired
    private APIControllerHelper helper;

    class Selection {
        final static String pbUrl = ""; // "http://www.reactome.org/PathwayBrowser/";

        Species species;
        Pathway pathway;
        Pathway subpathway;

        Selection(Pathway pathway, Pathway subpathway) {
            this.pathway = (Pathway) helper.getDetailedView("Pathway", pathway.getAvailableIdentifier());
            this.species = (Species) helper.getDetailedView("Species", this.pathway.getSpecies().get(0).getAvailableIdentifier());
            if(subpathway!=null){
                this.subpathway = (Pathway) helper.getDetailedView("Pathway", subpathway.getAvailableIdentifier());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Selection selection = (Selection) o;

            if (!pathway.equals(selection.pathway)) return false;
            if (!species.equals(selection.species)) return false;
            //noinspection RedundantIfStatement
            if (subpathway != null ? !subpathway.equals(selection.subpathway) : selection.subpathway != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = species.hashCode();
            result = 31 * result + pathway.hashCode();
            result = 31 * result + (subpathway != null ? subpathway.hashCode() : 0);
            return result;
        }

        public String generateLink(String resource) {
            //Resource based Identifier
            StringBuilder sb = new StringBuilder(resource);

            //REACTOME Identifier
            sb.append("\t");
            if(subpathway!=null){
                sb.append(subpathway.getAvailableIdentifier());
            }else{
                sb.append(pathway.getAvailableIdentifier());
            }

            //TOKEN
            sb.append("\t").append(pbUrl);
            if(!species.getDbId().equals(48887L)){
                sb.append("#SPECIES=").append(species.getAvailableIdentifier()).append("&");
            }else{
                sb.append("#");
            }
            sb.append("DIAGRAM=").append(pathway.getAvailableIdentifier());
            if(subpathway!=null){
                sb.append("&ID=").append(subpathway.getAvailableIdentifier());
            }

            //Pathway name
            String name = subpathway!=null?subpathway.getDisplayName():pathway.getDisplayName();
            boolean inferred = subpathway!=null?subpathway.getIsInferred():pathway.getIsInferred();
            sb.append("\t").append(name);

            //GO Evidence Codes
            //TAS: Traceable Author Statement
            //IEA: Inferred from Electronic Annotation
            sb.append("\t").append(inferred?"IEA":"TAS");

            //Species name
            sb.append("\t").append(species.getDisplayName());

            return sb.toString();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void export(MySQLAdaptor dba, ResourceFactory.MAIN resource){
        //THIS IS NEEDED HERE
        this.helper.setDba(dba);

        MapSet<AnalysisIdentifier, Long> resourceToPathways = new MapSet<AnalysisIdentifier, Long>();
        for (PhysicalEntityNode node : analysisData.getPhysicalEntityGraph().getRootNodes()) {
            resourceToPathways.addAll(getResourceToPathways(node, resource));
        }

        //Sorting the list to make it more professional xDD
        List<AnalysisIdentifier> identifiers = new LinkedList<AnalysisIdentifier>(resourceToPathways.keySet());
        Collections.sort(identifiers);

        MapSet<Long, PathwayNode> identifierMap = HierarchiesDataContainer.take().getPathwayLocation();
        for (AnalysisIdentifier identifier : identifiers) {
            Set<Selection> selections = new HashSet<Selection>();
            for (Long pathwayId : resourceToPathways.getElements(identifier)) {
                Set<PathwayNode> pathwayNodes = identifierMap.getElements(pathwayId);
                if(pathwayNodes!=null){
                    for (PathwayNode pathwayNode : pathwayNodes) {
                        String pId = pathwayNode.getPathwayId().toString();
                        String dId = pathwayNode.getDiagram().getPathwayId().toString();
                        Pathway subpathway = (Pathway) helper.getDetailedView("Pathway", pId);
                        Pathway pathway = (Pathway) helper.getDetailedView("Pathway", dId);
                        selections.add(new Selection(pathway, subpathway));
                    }
                }
            }
            for (Selection selection : selections) {
                System.out.println(selection.generateLink(identifier.toString()));
            }
        }
    }

    private MapSet<AnalysisIdentifier, Long> getResourceToPathways(PhysicalEntityNode node, ResourceFactory.MAIN resource){
        MapSet<AnalysisIdentifier, Long> rtn = new MapSet<AnalysisIdentifier, Long>();
        MainIdentifier identifier = node.getIdentifier();
        if(identifier!=null && identifier.is(resource)){
            rtn.add(identifier.getValue(), node.getPathwayIds());
        }
        for (PhysicalEntityNode child : node.getChildren()) {
            rtn.addAll(getResourceToPathways(child, resource));
        }
        return rtn;
    }
}