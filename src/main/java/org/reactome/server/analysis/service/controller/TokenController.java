package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.*;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.reactome.server.graph.domain.model.Species;
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
@Api(tags = "token", description = "Previous queries filter", position = 1)
@RequestMapping(value = "/token")
public class TokenController {

    private TokenUtils token;
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
                                   @RequestParam(required = false) Integer max) {
        List<Species> speciesList = analysis.getSpeciesList(species);
        return this.token.getFromToken(token)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
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
                                                        @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                                       @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                        @ApiParam(name = "pValue", value = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", defaultValue = "1")
                                                       @RequestParam(required = false, defaultValue = "1") Double pValue,
                                                        @ApiParam(name = "species", value = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                                       @RequestParam(required = false) String species,
                                                        @ApiParam(name = "includeDisease", value = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", defaultValue = "true")
                                                       @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                                        @ApiParam(name = "min", value = "minimum number of contained entities per pathway (takes into account the resource)")
                                                       @RequestParam(required = false) Integer min,
                                                        @ApiParam(name = "max", value = "maximum number of contained entities per pathway (takes into account the resource)")
                                                       @RequestParam(required = false) Integer max,
                                                        @ApiParam(name = "input", required = true, value = "A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)")
                                                       @RequestBody String input) {
        List<Species> speciesList = analysis.getSpeciesList(species);
        List<String> inputIdentifiers = analysis.getInputIdentifiers(input);
        return this.token.getFromToken(token)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max)
                .filterByPathways(inputIdentifiers, resource);
    }

    @ApiOperation(value = "Filters the result by species")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/filter/species/{species}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public SpeciesFilteredResult filterBySpecies( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                 @PathVariable String token,
                                                  @ApiParam(name = "species", required = true, value = "The species to filter the result (accepts the taxonomy id, species names and dbId)")
                                                 @PathVariable String species,
                                                  @ApiParam(name = "sortBy", value = "how to sort the result", defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_INTERACTORS,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_INTERACTORS,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                                 @RequestParam(required = false) String sortBy,
                                                  @ApiParam(name = "order", value = "specifies the order", defaultValue = "ASC", allowableValues = "ASC,DESC")
                                                 @RequestParam(required = false) String order,
                                                  @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                                 @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        Species s = analysis.getSpecies(species);
        return this.token.getFromToken(token).filterBySpecies(s.getDbId(), resource, sortBy, order);
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
                                 @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                 @ApiParam(name = "pValue", value = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", defaultValue = "1")
                                @RequestParam(required = false, defaultValue = "1") Double pValue,
                                 @ApiParam(name = "includeDisease", value = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", defaultValue = "true")
                                @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                 @ApiParam(name = "min", value = "minimum number of contained entities per pathway (takes into account the resource)")
                                @RequestParam(required = false) Integer min,
                                 @ApiParam(name = "max", value = "maximum number of contained entities per pathway (takes into account the resource)")
                                @RequestParam(required = false) Integer max) {
        return this.token.getFromToken(token)
                .filterPathways(resource, pValue, includeDisease, min, max)
                .getPage(pathway, sortBy, order, resource, pageSize);
    }

    @ApiOperation(value = "Returns a summary of the contained identifiers and interactors for a given pathway and token",
                  notes = "The identifiers submitted by the user that have a match in Reactome database. It also retrieves " +
                          "the mapping to the main identifiers for those that have been found.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/found/all/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public FoundElements getTokenHitEntitiesPathway( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                    @PathVariable String token,
                                                     @ApiParam(name = "pathway", required = true, value = "The identifier of the pathway of interest")
                                                    @PathVariable String pathway,
                                                     @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
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
    public List<FoundElements> getTokenHitEntitiesPathways( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                           @PathVariable String token,
                                                            @ApiParam(name = "input", required = true, value = "A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)")
                                                           @RequestBody String input,
                                                            @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
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
    public FoundEntities getTokenIdentifiersPathway( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                    @PathVariable String token,
                                                     @ApiParam(name = "pathway", required = true, value = "The identifier of the pathway of interest")
                                                    @PathVariable String pathway,
                                                     @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                                    @RequestParam(required = false) Integer page,
                                                     @ApiParam(name = "pageSize", value = "identifiers per page", defaultValue = "20")
                                                    @RequestParam(required = false) Integer pageSize,
                                                     @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
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
    public FoundInteractors getTokenInteractorsPathway( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                       @PathVariable String token,
                                                        @ApiParam(name = "pathway", required = true, value = "The identifier of the pathway of interest")
                                                       @PathVariable String pathway,
                                                        @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                                       @RequestParam(required = false) Integer page,
                                                        @ApiParam(name = "pageSize", value = "identifiers per page", defaultValue = "20")
                                                       @RequestParam(required = false) Integer pageSize,
                                                        @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
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
                                                     @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                                    @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                     @ApiParam(name = "pValue", value = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", defaultValue = "1")
                                                    @RequestParam(required = false, defaultValue = "1") Double pValue,
                                                     @ApiParam(name = "includeDisease", value = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", defaultValue = "true")
                                                    @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                                     @ApiParam(name = "min", value = "minimum number of contained entities per pathway (takes into account the resource)")
                                                    @RequestParam(required = false) Integer min,
                                                     @ApiParam(name = "max", value = "maximum number of contained entities per pathway (takes into account the resource)")
                                                    @RequestParam(required = false) Integer max) {
        return this.token.getFromToken(token).filterPathways(resource, pValue, includeDisease, min, max).getFoundReactions(pathway, resource);
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
                                                      @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                                     @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                                      @ApiParam(name = "pValue", value = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", defaultValue = "1")
                                                     @RequestParam(required = false, defaultValue = "1") Double pValue,
                                                      @ApiParam(name = "includeDisease", value = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", defaultValue = "true")
                                                     @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                                      @ApiParam(name = "min", value = "minimum number of contained entities per pathway (takes into account the resource)")
                                                     @RequestParam(required = false) Integer min,
                                                      @ApiParam(name = "max", value = "maximum number of contained entities per pathway (takes into account the resource)")
                                                     @RequestParam(required = false) Integer max) {
        List<String> pathwayIds = analysis.getInputIdentifiers(input);
        return this.token.getFromToken(token).filterPathways(resource, pValue, includeDisease, min, max).getFoundReactions(pathwayIds, resource);
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

    @ApiOperation(value = "Returns a list of binned hit pathway sizes associated with the token",
            notes = "Each bin has a key that determines the range by multiplying it by the binSize: [key x binSize - key+1 x binSize). For example, for a binSize of 100 and the range for the bin with key equals 8 is [800 - 900)")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/pathways/binned", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Bin> getPathwaysBinnedBySize( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                             @PathVariable String token,
                                              @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                             @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                              @ApiParam(name = "binSize", value = "defines the size of each bin for the classification (min: 100)", defaultValue = "100")
                                             @RequestParam(required = false, defaultValue = "100") Integer binSize,
                                              @ApiParam(name = "species", value = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
                                             @RequestParam(required = false) String species,
                                              @ApiParam(name = "pValue", value = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", defaultValue = "1")
                                             @RequestParam(required = false, defaultValue = "1") Double pValue,
                                              @ApiParam(name = "includeDisease", value = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", defaultValue = "true")
                                             @RequestParam(required = false, defaultValue = "true") Boolean includeDisease) {
        List<Species> speciesList = analysis.getSpeciesList(species);
        binSize = Math.max(binSize, 100);
        return this.token.getFromToken(token).getBinnedPathwaySize(binSize, resource, speciesList, pValue, includeDisease);
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
