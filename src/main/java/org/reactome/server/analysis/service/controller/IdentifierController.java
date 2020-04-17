package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.reactome.server.analysis.core.model.UserData;
import org.reactome.server.analysis.core.result.model.AnalysisResult;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.reactome.server.graph.domain.model.Species;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(tags="identifier", description = "Queries for only one identifier", position = 1)
@RequestMapping(value = "/identifier")
public class IdentifierController {

    private AnalysisHelper controller;

    @ApiOperation(value = "Analyse the identifier over the different species in the database and projects the result to Homo Sapiens",
                  notes = "The projection is calculated by the orthologous slot in the Reactome database. Use page and pageSize " +
                          "to reduce the amount of data retrieved. Use sortBy and order to sort the result by your preferred option. " +
                          "The resource field will filter the results to show only those corresponding to the preferred molecule type " +
                          "(TOTAL includes all the different molecules type)")
    @RequestMapping(value = "/{id}/projection", method = RequestMethod.GET , produces = "application/json")
    @ResponseBody
    public AnalysisResult getIdentifierToHuman( @ApiParam(name = "id", required = true, value = "The identifier of the element to be retrieved")
                                               @PathVariable String id,
                                                @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                               @RequestParam(required = false, defaultValue = "false") Boolean interactors,
                                                @ApiParam(name = "species", value = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                               @RequestParam(required = false) String species,
                                                @ApiParam(name = "pageSize", value = "pathways per page", defaultValue = "20")
                                               @RequestParam(required = false) Integer pageSize,
                                                @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                               @RequestParam(required = false) Integer page,
                                                @ApiParam(name = "sortBy", value = "how to sort the result", defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_INTERACTORS,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_INTERACTORS,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                               @RequestParam(required = false) String sortBy,
                                                @ApiParam(name = "order", value = "specifies the order", defaultValue = "ASC", allowableValues = "ASC,DESC")
                                               @RequestParam(required = false) String order,
                                                @ApiParam(name = "resource", value = "resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                               @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                @ApiParam(name = "pValue", value = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", defaultValue = "1")
                                               @RequestParam(required = false, defaultValue = "1") Double pValue,
                                                @ApiParam(name = "includeDisease", value = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", defaultValue = "true")
                                               @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                                @ApiParam(name = "min", value = "minimum number of contained entities per pathway (takes into account the resource)")
                                               @RequestParam(required = false) Integer min,
                                                @ApiParam(name = "max", value = "maximum number of contained entities per pathway (takes into account the resource)")
                                               @RequestParam(required = false) Integer max,
                                                HttpServletRequest request) {
        UserData ud = controller.getUserData(id);
        List<Species> speciesList = controller.getSpeciesList(species);
        return controller.analyse(ud, request, true, interactors, includeDisease)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @ApiOperation(value = "Analyse the identifier over the different species in the database",
                  notes = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                          "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                          "molecule type (TOTAL includes all the different molecules type)")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET , produces = "application/json")
    @ResponseBody
    public AnalysisResult getIdentifier( @ApiParam(name = "id" , required = true, value = "The identifier of the element to be retrieved")
                                       @PathVariable String id,
                                         @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                       @RequestParam(required = false, defaultValue = "false") Boolean interactors,
                                         @ApiParam(name = "species", value = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                       @RequestParam(required = false) String species,
                                        @ApiParam(name = "pageSize", value = "pathways per page", defaultValue = "20")
                                       @RequestParam(required = false) Integer pageSize,
                                        @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                       @RequestParam(required = false) Integer page,
                                        @ApiParam(name = "sortBy", value = "how to sort the result", defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_INTERACTORS,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_INTERACTORS,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                       @RequestParam(required = false) String sortBy,
                                        @ApiParam(name = "order", value = "specifies the order", defaultValue = "ASC", allowableValues = "ASC,DESC")
                                       @RequestParam(required = false) String order,
                                        @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                       @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                        @ApiParam(name = "pValue", value = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", defaultValue = "1")
                                       @RequestParam(required = false, defaultValue = "1") Double pValue,
                                        @ApiParam(name = "includeDisease", value = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", defaultValue = "true")
                                       @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                        @ApiParam(name = "min", value = "minimum number of contained entities per pathway (takes into account the resource)")
                                       @RequestParam(required = false) Integer min,
                                        @ApiParam(name = "max", value = "maximum number of contained entities per pathway (takes into account the resource)")
                                       @RequestParam(required = false) Integer max,
                                         HttpServletRequest request) {
        UserData ud = controller.getUserData(id);
        List<Species> speciesList = controller.getSpeciesList(species);
        return controller.analyse(ud, request, false, interactors, includeDisease)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @Autowired
    public void setController(AnalysisHelper controller) {
        this.controller = controller;
    }
}
