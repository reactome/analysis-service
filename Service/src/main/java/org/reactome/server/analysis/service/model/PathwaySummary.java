package org.reactome.server.analysis.service.model;

import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.service.result.PathwayNodeSummary;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
//@ApiModel(value = "PathwaySummary", description = "Contains general information about a certain pathway")
public class PathwaySummary {
//    @ApiModelProperty(value = "The pathway stable identifier", notes = "", required = true )
    private String stId;
//    @ApiModelProperty(value = "The pathway database identifier", notes = "", required = true )
    private Long dbId;
//    @ApiModelProperty(value = "The pathway name", notes = "", required = true )
    private String name;
//    @ApiModelProperty(value = "The pathway species", notes = "", required = true )
    private SpeciesSummary species;
//    @ApiModelProperty(value = "Whether it is a lower level pathway or not", notes = "", required = true )
    private boolean llp; //lower level pathway

//    private Long speciesDbId;

//    private String species;

//    @ApiModelProperty(value = "Statistics for the found entities in this pathway", notes = "", required = true )
    private EntityStatistics entities;
//    @ApiModelProperty(value = "Statistics for the found reactions in this pathway", notes = "", required = true )
    private ReactionStatistics reactions;

    public PathwaySummary(PathwayNodeSummary node, String resource, boolean interactors) {
        this.stId = node.getStId();
        this.dbId = node.getPathwayId();

        this.name = node.getName();
        this.species = new SpeciesSummary(node.getSpecies().getSpeciesID(), node.getSpecies().getName());
        this.llp = node.isLlp();
        initialize(node.getData(), resource, interactors);
    }

    private void initialize(PathwayNodeData d, String resource, boolean interactors){
        if(resource.equals("TOTAL")){
            this.entities = new EntityStatistics(d, interactors);
            this.reactions = new ReactionStatistics(d);
        }else{
            for (MainResource mr : d.getResources()) {
                if(mr.getName().equals(resource)){
                    this.entities = new EntityStatistics(mr, d, interactors);
                    this.reactions = new ReactionStatistics(mr, d);
                    break;
                }
            }
        }
    }

    public String getStId() {
        return stId;
    }

    public Long getDbId() {
        return dbId;
    }

    public String getName() {
        return name;
    }

    public SpeciesSummary getSpecies() {
        return species;
    }

    public boolean isLlp() {
        return llp;
    }

    public EntityStatistics getEntities() {
        return entities;
    }

    public ReactionStatistics getReactions() {
        return reactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathwaySummary that = (PathwaySummary) o;

        if (dbId != null ? !dbId.equals(that.dbId) : that.dbId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return dbId != null ? dbId.hashCode() : 0;
    }
}
