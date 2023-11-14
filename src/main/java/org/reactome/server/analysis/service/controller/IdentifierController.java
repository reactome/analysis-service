package org.reactome.server.analysis.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.model.UserData;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
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
@Tag(name = "identifier", description = "Queries for only one identifier")
@RequestMapping(value = "/identifier")
public class IdentifierController {

    private AnalysisHelper controller;

    @Operation(summary = "Analyse the identifier over the different species in the database and projects the result to Homo Sapiens",
            description = "The projection is calculated by the orthologous slot in the Reactome database. Use page and pageSize " +
                    "to reduce the amount of data retrieved. Use sortBy and order to sort the result by your preferred option. " +
                    "The resource field will filter the results to show only those corresponding to the preferred molecule type " +
                    "(TOTAL includes all the different molecules type)")
    @RequestMapping(value = "/{id}/projection", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public AnalysisResult getIdentifierToHuman(@Parameter(name = "id", required = true, description = "The identifier of the element to be retrieved")
                                               @PathVariable String id,
                                               @Parameter(name = "interactors", description = "Include interactors", example = "false")
                                               @RequestParam(required = false, defaultValue = "false") Boolean interactors,
                                               @Parameter(name = "species", description = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                               @RequestParam(required = false) String species,
                                               @Parameter(name = "pageSize", description = "pathways per page", example = "20")
                                               @RequestParam(required = false) Integer pageSize,
                                               @Parameter(name = "page", description = "page number", example = "1")
                                               @RequestParam(required = false) Integer page,
                                               @Parameter(name = "sortBy", schema = @Schema(description = "how to sort the result", example = "ENTITIES_PVALUE", allowableValues = {"NAME", "TOTAL_ENTITIES", "TOTAL_INTERACTORS", "TOTAL_REACTIONS", "FOUND_ENTITIES", "FOUND_INTERACTORS", "FOUND_REACTIONS", "ENTITIES_RATIO", "ENTITIES_PVALUE", "ENTITIES_FDR", "REACTIONS_RATIO"}))
                                               @RequestParam(required = false) String sortBy,
                                               @Parameter(name = "order", schema = @Schema(description = "specifies the order", example = "ASC", allowableValues = {"ASC", "DESC"}))
                                               @RequestParam(required = false) String order,
                                               @Parameter(name = "resource", schema = @Schema(description = "resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                               @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                               @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                               @RequestParam(required = false, defaultValue = "1") Double pValue,
                                               @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", example = "true")
                                               @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                               @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                               @RequestParam(required = false) Integer min,
                                               @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                               @RequestParam(required = false) Integer max,
                                               @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                               @RequestParam(required = false, defaultValue = "false") Boolean importableOnly,
                                               HttpServletRequest request) {
        UserData ud = controller.getUserData(id);
        List<Species> speciesList = controller.getSpeciesList(species);
        return controller.analyse(ud, request, true, interactors, includeDisease)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max, importableOnly)
                .getResultSummary(sortBy, order, resource, pageSize, page, importableOnly);
    }

    @Operation(summary = "Analyse the identifier over the different species in the database",
            description = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                    "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                    "molecule type (TOTAL includes all the different molecules type)")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public AnalysisResult getIdentifier(@Parameter(name = "id", required = true, description = "The identifier of the element to be retrieved")
                                        @PathVariable String id,
                                        @Parameter(name = "interactors", description = "Include interactors", example = "false")
                                        @RequestParam(required = false, defaultValue = "false") Boolean interactors,
                                        @Parameter(name = "species", description = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                        @RequestParam(required = false) String species,
                                        @Parameter(name = "pageSize", description = "pathways per page", example = "20")
                                        @RequestParam(required = false) Integer pageSize,
                                        @Parameter(name = "page", description = "page number", example = "1")
                                        @RequestParam(required = false) Integer page,
                                        @Parameter(name = "sortBy", schema = @Schema(description = "how to sort the result", example = "ENTITIES_PVALUE", allowableValues = {"NAME", "TOTAL_ENTITIES", "TOTAL_INTERACTORS", "TOTAL_REACTIONS", "FOUND_ENTITIES", "FOUND_INTERACTORS", "FOUND_REACTIONS", "ENTITIES_RATIO", "ENTITIES_PVALUE", "ENTITIES_FDR", "REACTIONS_RATIO"}))
                                        @RequestParam(required = false) String sortBy,
                                        @Parameter(name = "order", schema = @Schema(description = "specifies the order", example = "ASC", allowableValues = {"ASC", "DESC"}))
                                        @RequestParam(required = false) String order,
                                        @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                        @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                        @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                        @RequestParam(required = false, defaultValue = "1") Double pValue,
                                        @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", example = "true")
                                        @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                        @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                        @RequestParam(required = false) Integer min,
                                        @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                        @RequestParam(required = false) Integer max,
                                        @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                        @RequestParam(required = false, defaultValue = "false") Boolean importableOnly,
                                        HttpServletRequest request) {
        UserData ud = controller.getUserData(id);
        List<Species> speciesList = controller.getSpeciesList(species);
        return controller.analyse(ud, request, false, interactors, includeDisease)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max, importableOnly)
                .getResultSummary(sortBy, order, resource, pageSize, page, importableOnly);
    }

    @Autowired
    public void setController(AnalysisHelper controller) {
        this.controller = controller;
    }
}
