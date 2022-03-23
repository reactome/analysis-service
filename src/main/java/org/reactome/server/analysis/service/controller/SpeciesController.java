package org.reactome.server.analysis.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.model.SpeciesNodeFactory;
import org.reactome.server.analysis.core.result.model.AnalysisResult;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@Tag(name = "species", description = "Species comparison")
@RequestMapping(value = "/species")
public class SpeciesController {

    @Autowired
    private AnalysisHelper controller;

    @Operation(summary = "Compares Homo sapiens to the specified species",
            description = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                    "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                    "molecule type (TOTAL includes all the different molecules type)")
    @ApiResponses({@ApiResponse(responseCode = "404", description = "Species identifier does not match with any of the species in the current data")})
    @RequestMapping(value = "/homoSapiens/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public AnalysisResult compareHomoSapiensTo(@Parameter(name = "species", required = true, description = "The dbId of the species to compare to")
                                               @PathVariable Long species,
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
                                               @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                               @RequestParam(required = false) Integer min,
                                               @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                               @RequestParam(required = false) Integer max,
                                               HttpServletRequest request) {
        Long from = SpeciesNodeFactory.getHumanNode().getSpeciesID(); //For the time being let's do only human ;)
        return controller.compareSpecies(from, species, request)
                .filterPathways(resource, pValue, true, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }


}

