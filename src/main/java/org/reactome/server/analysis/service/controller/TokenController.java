package org.reactome.server.analysis.service.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.reactome.server.graph.domain.model.Species;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Tag(name = "token", description = "Previous queries filter")
@RequestMapping(value = "/token")
public class TokenController {

    private TokenUtils token;
    private AnalysisHelper analysis;

    @Operation(summary = "Returns the result associated with the token",
            description = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                    "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                    "molecule type (TOTAL includes all the different molecules type)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public AnalysisResult getToken(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                   @PathVariable String token,
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
                                   @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)")
                                   @RequestParam(required = false) Boolean includeDisease,
                                   @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                   @RequestParam(required = false) Integer min,
                                   @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                   @RequestParam(required = false) Integer max,
                                   @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                   @RequestParam(required = false, defaultValue = "false") Boolean importableOnly) {
        List<Species> speciesList = analysis.getSpeciesList(species);
        AnalysisStoredResult asr = this.token.getFromToken(token);

        if (includeDisease != null) asr.getSummary().setIncludeDisease(includeDisease);
        else includeDisease = asr.getSummary().isIncludeDisease();

        return asr.filterPathways(speciesList, resource, pValue, includeDisease, min, max, importableOnly)
                .getResultSummary(sortBy, order, resource, pageSize, page, importableOnly);
    }

    @Operation(summary = "Returns the result for the pathway ids sent by post (when they are present in the original result)",
            description = "For a given list of pathway identifiers it will retrieve a list containing those that are " +
                    "present in the result (with the results for the indicated molecule type)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/filter/pathways", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<PathwaySummary> getTokenFilterPathways(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                       @PathVariable String token,
                                                       @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                       @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                       @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                                       @RequestParam(required = false, defaultValue = "1") Double pValue,
                                                       @Parameter(name = "species", description = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                                       @RequestParam(required = false) String species,
                                                       @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)")
                                                       @RequestParam(required = false) Boolean includeDisease,
                                                       @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                                       @RequestParam(required = false) Integer min,
                                                       @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                                       @RequestParam(required = false) Integer max,
                                                       @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                                       @RequestParam(required = false, defaultValue = "false") Boolean importableOnly,
                                                       @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                               description = "<b>input</b> A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)",
                                                               required = true
                                                       )
                                                       @RequestBody String input) {
        List<Species> speciesList = analysis.getSpeciesList(species);
        List<String> inputIdentifiers = analysis.getInputIdentifiers(input);
        AnalysisStoredResult asr = this.token.getFromToken(token);

        if (includeDisease != null) asr.getSummary().setIncludeDisease(includeDisease);
        else includeDisease = asr.getSummary().isIncludeDisease();

        return asr.filterPathways(speciesList, resource, pValue, includeDisease, min, max, importableOnly)
                .filterByPathways(inputIdentifiers, resource, importableOnly);
    }

    @Operation(summary = "Filters the result by species")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/filter/species/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public SpeciesFilteredResult filterBySpecies(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                 @PathVariable String token,
                                                 @Parameter(name = "species", required = true, description = "The species to filter the result (accepts the taxonomy id, species names and dbId)")
                                                 @PathVariable String species,
                                                 @Parameter(name = "sortBy", schema = @Schema(description = "how to sort the result", example = "ENTITIES_PVALUE", allowableValues = {"NAME", "TOTAL_ENTITIES", "TOTAL_INTERACTORS", "TOTAL_REACTIONS", "FOUND_ENTITIES", "FOUND_INTERACTORS", "FOUND_REACTIONS", "ENTITIES_RATIO", "ENTITIES_PVALUE", "ENTITIES_FDR", "REACTIONS_RATIO"}))
                                                 @RequestParam(required = false) String sortBy,
                                                 @Parameter(name = "order", schema = @Schema(description = "specifies the order", example = "ASC", allowableValues = {"ASC", "DESC"}))
                                                 @RequestParam(required = false) String order,
                                                 @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                 @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                 @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                                 @RequestParam(required = false, defaultValue = "false") Boolean importableOnly) {
        Species s = analysis.getSpecies(species);
        return this.token.getFromToken(token).filterBySpecies(s.getDbId(), resource, sortBy, order, importableOnly);
    }

    @Operation(summary = "Returns the page where the corresponding pathway is taking into account the passed parameters",
            description = "Useful when implementing UI with tables showing the results in a page way and the user needs " +
                    "to know in which page a certain pathway is present for a given set of sorting and filtering " +
                    "options.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/page/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public int getPageOfPathway(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                @PathVariable String token,
                                @Parameter(name = "pathway", required = true, description = "The database identifier of the pathway of interest")
                                @PathVariable String pathway,
                                @Parameter(name = "pageSize", description = "pathways per page", example = "20")
                                @RequestParam(required = false) Integer pageSize,
                                @Parameter(name = "sortBy", schema = @Schema(description = "how to sort the result", example = "ENTITIES_PVALUE", allowableValues = {"NAME", "TOTAL_ENTITIES", "TOTAL_INTERACTORS", "TOTAL_REACTIONS", "FOUND_ENTITIES", "FOUND_INTERACTORS", "FOUND_REACTIONS", "ENTITIES_RATIO", "ENTITIES_PVALUE", "ENTITIES_FDR", "REACTIONS_RATIO"}))
                                @RequestParam(required = false) String sortBy,
                                @Parameter(name = "order", schema = @Schema(description = "specifies the order", example = "ASC", allowableValues = {"ASC", "DESC"}))
                                @RequestParam(required = false) String order,
                                @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                @RequestParam(required = false, defaultValue = "1") Double pValue,
                                @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)")
                                @RequestParam(required = false) Boolean includeDisease,
                                @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                @RequestParam(required = false) Integer min,
                                @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                @RequestParam(required = false) Integer max,
                                @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                @RequestParam(required = false, defaultValue = "false") Boolean importableOnly) {

        AnalysisStoredResult asr = this.token.getFromToken(token);

        if (includeDisease != null) asr.getSummary().setIncludeDisease(includeDisease);
        else includeDisease = asr.getSummary().isIncludeDisease();

        return asr.filterPathways(resource, pValue, includeDisease, min, max, importableOnly)
                .getPage(pathway, sortBy, order, resource, pageSize);
    }

    @Operation(summary = "Returns a summary of the contained identifiers and interactors for a given pathway and token",
            description = "The identifiers submitted by the user that have a match in Reactome database. It also retrieves " +
                    "the mapping to the main identifiers for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/all/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundElements getTokenHitEntitiesPathway(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                    @PathVariable String token,
                                                    @Parameter(name = "pathway", required = true, description = "The identifier of the pathway of interest")
                                                    @PathVariable String pathway,
                                                    @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                    @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        FoundElements fe = this.token.getFromToken(token).getFoundElmentsForPathway(pathway, resource);
        if (fe == null) throw new ResourceNotFoundException();
        return fe;
    }

    @Operation(summary = "Returns a summary of the contained identifiers and interactors for each requested pathway and a given token",
            description = "The identifiers submitted by the user that have a match in Reactome database. It also retrieves " +
                    "the mapping to the main identifiers for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/all", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<FoundElements> getTokenHitEntitiesPathways(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                           @PathVariable String token,
                                                           @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                   description = "<b>input</b> A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)",
                                                                   required = true
                                                           )
                                                           @RequestBody String input,
                                                           @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                           @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        List<String> pathways = analysis.getInputIdentifiers(input);
        List<FoundElements> fes = this.token.getFromToken(token).getFoundElmentsForPathways(pathways, resource);
        if (fes == null || fes.isEmpty()) throw new ResourceNotFoundException();
        return fes;
    }

    @Operation(summary = "Returns a summary of the found curated identifiers for a given pathway and token",
            description = "The identifiers submitted by the user that have a match in Reactome database. It also retrieves " +
                    "the mapping to the main identifiers for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/entities/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundEntities getTokenIdentifiersPathway(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                    @PathVariable String token,
                                                    @Parameter(name = "pathway", required = true, description = "The identifier of the pathway of interest")
                                                    @PathVariable String pathway,
                                                    @Parameter(name = "page", description = "page number", example = "1")
                                                    @RequestParam(required = false) Integer page,
                                                    @Parameter(name = "pageSize", description = "identifiers per page", example = "20")
                                                    @RequestParam(required = false) Integer pageSize,
                                                    @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                    @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        FoundEntities fi = this.token.getFromToken(token).getFoundEntities(pathway, resource);
        if (fi == null) throw new ResourceNotFoundException();
        return fi.filter(resource, pageSize, page);
    }

    /*
     We keep this method for backwards compatibility but the idea is to get rid of it in future versions
     */
    @Hidden
    @Deprecated
    @RequestMapping(value = "/{token}/summary/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundEntities getTokenSummaryPathway(@PathVariable String token, @PathVariable String pathway,
                                                @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return getTokenIdentifiersPathway(token, pathway, page, pageSize, resource);
    }

    @Operation(summary = "Returns a summary of the found interactors for a given pathway and token",
            description = "The identifiers submitted by the user that have a match with an interactor in Reactome database. It also retrieves " +
                    "the mapping to the main identifiers (the one interacting with) for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/interactors/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundInteractors getTokenInteractorsPathway(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                       @PathVariable String token,
                                                       @Parameter(name = "pathway", required = true, description = "The identifier of the pathway of interest")
                                                       @PathVariable String pathway,
                                                       @Parameter(name = "page", description = "page number", example = "1")
                                                       @RequestParam(required = false) Integer page,
                                                       @Parameter(name = "pageSize", description = "identifiers per page", example = "20")
                                                       @RequestParam(required = false) Integer pageSize,
                                                       @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                       @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        FoundInteractors fi = this.token.getFromToken(token).getFoundInteractors(pathway);
        if (fi == null || fi.getFound() == 0) {
            throw new ResourceNotFoundException();
        }
        return fi.filter(resource, pageSize, page);
    }


    @Operation(summary = "Returns a list of the identifiers not found for a given token",
            description = "Those identifiers that have not been found in the Reactome database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/notFound", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<IdentifierSummary> getNotFoundIdentifiers(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                          @PathVariable String token,
                                                          @Parameter(name = "pageSize", description = "identifiers per page", example = "40")
                                                          @RequestParam(required = false) Integer pageSize,
                                                          @Parameter(name = "page", description = "page number", example = "1")
                                                          @RequestParam(required = false) Integer page) {
        List<IdentifierSummary> notFound = this.token.getFromToken(token).getNotFoundIdentifiers();
        return analysis.filter(notFound, pageSize, page);
    }

    @Operation(summary = "Returns the reaction ids of the provided pathway id that are present in the original result",
            description = "For a given pathway it returns the identifiers (dbId) of the reactions in the pathway that " +
                    "have been hit with the sample taking into account their participating molecules.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/reactions/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Set<Long> getTokenFilterPathwayReactions(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                    @PathVariable String token,
                                                    @Parameter(name = "pathway", required = true, description = "The database identifier of the pathway of interest")
                                                    @PathVariable String pathway,
                                                    @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                    @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                    @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                                    @RequestParam(required = false, defaultValue = "1") Double pValue,
                                                    @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)")
                                                    @RequestParam(required = false) Boolean includeDisease,
                                                    @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                                    @RequestParam(required = false) Integer min,
                                                    @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                                    @RequestParam(required = false) Integer max,
                                                    @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                                    @RequestParam(required = false, defaultValue = "false") Boolean importableOnly) {

        AnalysisStoredResult asr = this.token.getFromToken(token);

        if (includeDisease != null) asr.getSummary().setIncludeDisease(includeDisease);
        else includeDisease = asr.getSummary().isIncludeDisease();

        return asr.filterPathways(resource, pValue, includeDisease, min, max, importableOnly)
                .getFoundReactions(pathway, resource, importableOnly);
    }

    @Operation(summary = "Returns the reaction ids of the pathway ids sent by post that are present in the original result",
            description = "It filters the submitted list and retrieves back only those that at least one of the participating " +
                    "molecules has been hit with the user submitted data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/reactions/pathways", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Set<Long> getTokenFilterPathwaysReactions(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                     @PathVariable String token,
                                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                             description = "<b>input</b> A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)",
                                                             required = true
                                                     )
                                                     @RequestBody String input,
                                                     @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                     @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                     @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                                     @RequestParam(required = false, defaultValue = "1") Double pValue,
                                                     @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)")
                                                     @RequestParam(required = false) Boolean includeDisease,
                                                     @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                                     @RequestParam(required = false) Integer min,
                                                     @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                                     @RequestParam(required = false) Integer max,
                                                     @Parameter(name = "importableOnly", description = "Filters resources to only includes importable ones")
                                                     @RequestParam(required = false, defaultValue = "false") Boolean importableOnly) {
        List<String> pathwayIds = analysis.getInputIdentifiers(input);
        AnalysisStoredResult asr = this.token.getFromToken(token);

        if (includeDisease != null) asr.getSummary().setIncludeDisease(includeDisease);
        else includeDisease = asr.getSummary().isIncludeDisease();

        return asr.filterPathways(resource, pValue, includeDisease, min, max, importableOnly)
                .getFoundReactions(pathwayIds, resource, importableOnly);
    }

    @Operation(summary = "Returns the resources summary associated with the token",
            description = "A summary of the molecules type associated to the submitted data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ResourceSummary> getResources(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                              @PathVariable String token) {
        return this.token.getFromToken(token).getResourceSummary();
    }

    @Operation(summary = "Returns a list of binned hit pathway sizes associated with the token",
            description = "Each bin has a key that determines the range by multiplying it by the binSize: [key x binSize - key+1 x binSize). For example, for a binSize of 100 and the range for the bin with key equals 8 is [800 - 900)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/pathways/binned", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Bin> getPathwaysBinnedBySize(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                             @PathVariable String token,
                                             @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                             @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                             @Parameter(name = "binSize", schema = @Schema(description = "defines the size of each bin for the classification (min: 100)", example = "100"))
                                             @RequestParam(required = false, defaultValue = "100") Integer binSize,
                                             @Parameter(name = "species", description = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                             @RequestParam(required = false) String species,
                                             @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                             @RequestParam(required = false, defaultValue = "1") Double pValue,
                                             @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)")
                                             @RequestParam(required = false) Boolean includeDisease) {
        List<Species> speciesList = analysis.getSpeciesList(species);
        binSize = Math.max(binSize, 100);
        AnalysisStoredResult asr = this.token.getFromToken(token);

        if (includeDisease != null) asr.getSummary().setIncludeDisease(includeDisease);
        else includeDisease = asr.getSummary().isIncludeDisease();

        return asr.getBinnedPathwaySize(binSize, resource, speciesList, pValue, includeDisease);
    }

    @Autowired
    public void setToken(TokenUtils token) {
        this.token = token;
    }

    @Autowired
    public void setAnalysis(AnalysisHelper analysis) {
        this.analysis = analysis;
    }
}
