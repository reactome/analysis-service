package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.*;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(tags = "token", description = "Previous queries filter", position = 2)
@RequestMapping(value = "/token")
public class TokenController {

    @Autowired
    private TokenUtils token;

    @Autowired
    private AnalysisHelper analysis;

    @ApiOperation(value = "Returns the result associated with the token",
                  notes = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                          "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                          "molecule type (TOTAL includes all the different molecules type)")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public AnalysisResult getToken( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                  @PathVariable String token,
                                   @ApiParam(name = "pageSize", value = "pathways per page", defaultValue = "20")
                                  @RequestParam(required = false) Integer pageSize,
                                   @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                  @RequestParam(required = false) Integer page,
                                    @ApiParam(name = "sortBy", value = "how to sort the result", defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_INTERACTORS,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_INTERACTORS,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                  @RequestParam(required = false) String sortBy,
                                    @ApiParam(name = "order", value = "specifies the order", defaultValue = "ASC", allowableValues = "ASC,DESC")
                                  @RequestParam(required = false) String order,
                                    @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                  @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return this.token.getFromToken(token).getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @ApiOperation(value = "Returns the result for the pathway ids sent by post (when they are present in the original result)",
                  notes = "For a given list of pathway identifiers it will retrieve a list containing those that are " +
                          "present in the result (with the results for the indicated molecule type)")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/filter/pathways", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<PathwaySummary> getTokenFilterPathways( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                @PathVariable String token,
                                                 @ApiParam(name = "input", required = true, value = "A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)")
                                                @RequestBody String input,
                                                 @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                                @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        List<String> inputIdentifiers = analysis.getInputIdentifiers(input);
        return this.token.getFromToken(token).filterByPathways(inputIdentifiers, resource);
    }

    @ApiOperation(value = "Filters the result by species")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/filter/species/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public SpeciesFilteredResult filterBySpecies( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                          @PathVariable String token,
                                           @ApiParam(name = "species", required = true, value = "The dbId of the species to filter the result")
                                          @PathVariable Long species,
                                           @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                          @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return this.token.getFromToken(token).filterBySpecies(species, resource);
    }

    @ApiOperation(value = "Returns the page where the corresponding pathway is taking into account the passed parameters",
                  notes = "Useful when implementing UI with tables showing the results in a page way and the user needs " +
                          "to know in which page a certain pathway is present for a given set of sorting and filtering " +
                          "options.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/page/{pathway}", method = RequestMethod.GET , produces = "application/json")
    @ResponseBody
    public int getPageOfPathway( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                @PathVariable String token,
                                 @ApiParam(name = "pathway", required = true, value = "The database identifier of the pathway of interest")
                                @PathVariable String pathway,
                                 @ApiParam(name = "pageSize", value = "pathways per page", defaultValue = "20")
                                @RequestParam(required = false) Integer pageSize,
                                 @ApiParam(name = "sortBy", value = "how to sort the result", defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_INTERACTORS,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_INTERACTORS,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                @RequestParam(required = false) String sortBy,
                                 @ApiParam(name = "order", value = "specifies the order", defaultValue = "ASC", allowableValues = "ASC,DESC")
                                @RequestParam(required = false) String order,
                                 @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                 @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return this.token.getFromToken(token).getPage(pathway, sortBy, order, resource, pageSize);
    }

    @ApiOperation(value = "Returns a summary of the contained identifiers and interactors for a given pathway and token",
                  notes = "The identifiers submitted by the user that have a match in Reactome database. It also retrieves " +
                          "the mapping to the main identifiers for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/all/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundElements getTokenHitEntitiesPathway(@ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                  @PathVariable String token,
                                                    @ApiParam(name = "pathway", required = true, value = "The identifier of the pathway of interest")
                                                  @PathVariable String pathway,
                                                    @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                                  @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        FoundElements fe = this.token.getFromToken(token).getFoundElmentsForPathway(pathway, resource);
        if(fe == null) throw new ResourceNotFoundException();
        return fe;
    }

    @ApiOperation(value = "Returns a summary of the contained identifiers and interactors for each requested pathway and a given token",
            notes = "The identifiers submitted by the user that have a match in Reactome database. It also retrieves " +
                    "the mapping to the main identifiers for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/all", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<FoundElements> getTokenHitEntitiesPathways(@ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                          @PathVariable String token,
                                                           @ApiParam(name = "input", required = true, value = "A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)")
                                                          @RequestBody String input,
                                                           @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                                          @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        List<String> pathways = analysis.getInputIdentifiers(input);
        List<FoundElements> fes = this.token.getFromToken(token).getFoundElmentsForPathways(pathways, resource);
        if(fes == null || fes.isEmpty()) throw new ResourceNotFoundException();
        return fes;
    }

    @ApiOperation(value = "Returns a summary of the found curated identifiers for a given pathway and token",
            notes = "The identifiers submitted by the user that have a match in Reactome database. It also retrieves " +
                    "the mapping to the main identifiers for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/entities/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundEntities getTokenIdentifiersPathway(@ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                  @PathVariable String token,
                                                    @ApiParam(name = "pathway", required = true, value = "The identifier of the pathway of interest")
                                                  @PathVariable String pathway,
                                                    @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                                  @RequestParam(required = false) Integer page,
                                                    @ApiParam(name = "pageSize", value = "identifiers per page", defaultValue = "20")
                                                  @RequestParam(required = false) Integer pageSize,
                                                    @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                                  @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        FoundEntities fi = this.token.getFromToken(token).getFoundEntities(pathway);
        if (fi == null) throw new ResourceNotFoundException();
        return fi.filter(resource, pageSize, page);
    }

    /*
     We keep this method for backwards compatibility but the idea is to get rid of it in future versions
     */
    @ApiIgnore
    @Deprecated
    @RequestMapping(value = "/{token}/summary/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundEntities getTokenSummaryPathway(@PathVariable String token, @PathVariable String pathway,
                                                @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return getTokenIdentifiersPathway(token, pathway, page, pageSize, resource);
    }

    @ApiOperation(value = "Returns a summary of the found interactors for a given pathway and token",
            notes = "The identifiers submitted by the user that have a match with an interactor in Reactome database. It also retrieves " +
                    "the mapping to the main identifiers (the one interacting with) for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/interactors/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundInteractors getTokenInteractorsPathway(@ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                         @PathVariable String token,
                                                       @ApiParam(name = "pathway", required = true, value = "The identifier of the pathway of interest")
                                                         @PathVariable String pathway,
                                                       @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                                         @RequestParam(required = false) Integer page,
                                                       @ApiParam(name = "pageSize", value = "identifiers per page", defaultValue = "20")
                                                         @RequestParam(required = false) Integer pageSize,
                                                       @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                                         @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        FoundInteractors fi = this.token.getFromToken(token).getFoundInteractors(pathway);
        if (fi == null) throw new ResourceNotFoundException();
        return fi.filter(resource, pageSize, page);
    }





    @ApiOperation(value = "Returns a list of the identifiers not found for a given token",
                  notes = "Those identifiers that have not been found in the Reactome database")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/notFound", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<IdentifierSummary> getNotFoundIdentifiers( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                          @PathVariable String token,
                                                           @ApiParam(name = "pageSize", value = "identifiers per page", defaultValue = "40")
                                                          @RequestParam(required = false) Integer pageSize,
                                                           @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                                          @RequestParam(required = false) Integer page) {
        List<IdentifierSummary> notFound = this.token.getFromToken(token).getNotFoundIdentifiers();
        return analysis.filter(notFound, pageSize, page);
    }

    @ApiOperation(value = "Returns the reaction ids of the provided pathway id that are present in the original result",
                  notes = "For a given pathway it returns the identifiers (dbId) of the reactions in the pathway that " +
                          "have been hit with the sample taking into account their participating molecules.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/reactions/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Set<Long> getTokenFilterPathwayReactions( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                             @PathVariable String token,
                                              @ApiParam(name = "pathway", required = true, value = "The database identifier of the pathway of interest")
                                             @PathVariable String pathway,
                                              @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                             @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return this.token.getFromToken(token).getFoundReactions(pathway, resource);
    }

    @ApiOperation(value = "Returns the reaction ids of the pathway ids sent by post that are present in the original result",
                  notes = "It filters the submitted list and retrieves back only those that at least one of the participating " +
                          "molecules has been hit with the user submitted data.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/reactions/pathways", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public Set<Long> getTokenFilterPathwaysReactions( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                             @PathVariable String token,
                                              @ApiParam(name = "input", required = true, value = "A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)")
                                             @RequestBody String input,
                                              @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                             @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        List<String> pathwayIds = analysis.getInputIdentifiers(input);
        return this.token.getFromToken(token).getFoundReactions(pathwayIds, resource);
    }

    @ApiOperation(value = "Returns the resources summary associated with the token",
                  notes = "A summary of the molecules type associated to the submitted data.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ResourceSummary> getResources( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                              @PathVariable String token) {
        return this.token.getFromToken(token).getResourceSummary();
    }
}
