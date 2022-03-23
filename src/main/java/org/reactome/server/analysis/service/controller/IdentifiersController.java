package org.reactome.server.analysis.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.model.UserData;
import org.reactome.server.analysis.core.result.model.AnalysisResult;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.reactome.server.graph.domain.model.Species;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Tag(name = "identifiers", description = "Queries for multiple identifiers")
@RequestMapping(value = "/identifiers")
public class IdentifiersController {

    private AnalysisHelper controller;

    @Operation(summary = "Analyse the post identifiers over the different species and projects the result to Homo Sapiens",
            description = "The projection is calculated by the orthologous slot in the Reactome database. Use page and pageSize " +
                    "to reduce the amount of data retrieved. Use sortBy and order to sort the result by your preferred option. " +
                    "The resource field will filter the results to show only those corresponding to the preferred molecule type " +
                    "(TOTAL includes all the different molecules type)")
    @ApiResponses({@ApiResponse(responseCode = "400", description = "Bad request")})
    @RequestMapping(value = "/projection", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostTextToHuman(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>input</b> Identifiers to analyse followed by their expression (when applies)",
                    required = true
            )
            @RequestBody String input,
            @Parameter(name = "interactors", description = "Include interactors", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean interactors,
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
            HttpServletRequest request) {
        UserData ud = controller.getUserData(input);
        return controller.analyse(ud, request, true, interactors, includeDisease)
                .filterPathways(resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @Operation(summary = "Analyse the post identifiers over the different species",
            description = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                    "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                    "molecule type (TOTAL includes all the different molecules type)")
    @ApiResponses({@ApiResponse(responseCode = "400", description = "Bad request")})
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostText(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>Name</b> Identifiers to analyse followed by their expression (when applies)",
                    required = true)
            @RequestBody String input,
            @Parameter(name = "interactors", description = "Include interactors", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean interactors,
            @Parameter(name = "species", description = "list of species to filter the result (accepts taxonomy ids, species names and dbId)")
            @RequestParam(required = false) String species,
            @Parameter(name = "pageSize", description = "pathways per page", example = "20")
            @RequestParam(required = false) Integer pageSize,
            @Parameter(name = "page", description = "page number", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(name = "sortBy", schema = @Schema(description = "how to sort the result", example = " ENTITIES_PVALUE", allowableValues = {"NAME", "TOTAL_ENTITIES", "TOTAL_INTERACTORS", "TOTAL_REACTIONS", "FOUND_ENTITIES", "FOUND_INTERACTORS", "FOUND_REACTIONS", "ENTITIES_RATIO", "ENTITIES_PVALUE", "ENTITIES_FDR", "REACTIONS_RATIO"}))
            @RequestParam(required = false) String sortBy,
            @Parameter(name = "order", schema = @Schema(description = "specifies the order", example = "ASC", allowableValues = {"ASC", "DESC"}))
            @RequestParam(required = false) String order,
            @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL","COMPOUND", "PUBCHEM_COMPOUND"}))
            @RequestParam(required = false, defaultValue = "TOTAL") String resource,
            @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
            @RequestParam(required = false, defaultValue = "1") Double pValue,
            @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", example = "true")
            @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
            @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
            @RequestParam(required = false) Integer min,
            @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
            @RequestParam(required = false) Integer max,
            HttpServletRequest request) {
        UserData ud = controller.getUserData(input);
        List<Species> speciesList = controller.getSpeciesList(species);
        return controller.analyse(ud, request, false, interactors, includeDisease)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }


    @Operation(summary = "Analyse the identifiers in the file over the different species and projects the result to Homo Sapiens",
            description = "The projection is calculated by the orthologous slot in the Reactome database. Use page and pageSize " +
                    "to reduce the amount of data retrieved. Use sortBy and order to sort the result by your preferred option. " +
                    "The resource field will filter the results to show only those corresponding to the preferred molecule type " +
                    "(TOTAL includes all the different molecules type)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')")})
    @RequestMapping(value = "/form/projection", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostFileToHuman(@Parameter(name = "file", required = true, description = "A file with the data to be analysed")
                                             @RequestPart MultipartFile file,
                                             @Parameter(name = "interactors", description = "Include interactors", example = "false")
                                             @RequestParam(required = false, defaultValue = "false") Boolean interactors,
                                             @Parameter(name = "pageSize", description = "pathways per page", example = "20")
                                             @RequestParam(required = false) Integer pageSize,
                                             @Parameter(name = "page", description = "page number", example = "1")
                                             @RequestParam(required = false) Integer page,
                                             @Parameter(name = "sortBy", schema = @Schema(description = "how to sort the result", example = "ENTITIES_PVALUE", allowableValues = {"NAME", "TOTAL_ENTITIES", "TOTAL_INTERACTORS", "TOTAL_REACTIONS", "FOUND_ENTITIES", "FOUND_INTERACTORS", "FOUND_REACTIONS", "ENTITIES_RATIO", "ENTITIES_PVALUE", "ENTITIES_FDR", "REACTIONS_RATIO"}))
                                             @RequestParam(required = false) String sortBy,
                                             @Parameter(name = "order", schema = @Schema(description = "specifies the order", example = "ASC", allowableValues = {"ASC", "DESC"}))
                                             @RequestParam(required = false) String order,
                                             @Parameter(name = "resource", schema = @Schema(description = "the resource to sort", example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL","COMPOUND", "PUBCHEM_COMPOUND"}))
                                             @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                             @Parameter(name = "pValue", description = "defines the pValue threshold. Only hit pathway with pValue equals or below the threshold will be returned", example = "1")
                                             @RequestParam(required = false, defaultValue = "1") Double pValue,
                                             @Parameter(name = "includeDisease", description = "set to 'false' to exclude the disease pathways from the result (it does not alter the statistics)", example = "true")
                                             @RequestParam(required = false, defaultValue = "true") Boolean includeDisease,
                                             @Parameter(name = "min", description = "minimum number of contained entities per pathway (takes into account the resource)")
                                             @RequestParam(required = false) Integer min,
                                             @Parameter(name = "max", description = "maximum number of contained entities per pathway (takes into account the resource)")
                                             @RequestParam(required = false) Integer max,
                                             HttpServletRequest request) {
        UserData ud = controller.getUserData(file);
        return controller.analyse(ud, request, true, interactors, file.getOriginalFilename(), includeDisease)
                .filterPathways(resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @Operation(summary = "Analyse the identifiers in the file over the different species",
            description = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                    "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                    "molecule type (TOTAL includes all the different molecules type)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')")})
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostFile(@Parameter(name = "file", required = true, description = "A file with the data to be analysed")
                                      @RequestPart MultipartFile file,
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
                                      HttpServletRequest request) {
        UserData ud = controller.getUserData(file);
        List<Species> speciesList = controller.getSpeciesList(species);
        return controller.analyse(ud, request, false, interactors, file.getOriginalFilename(), includeDisease)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @Operation(summary = "Analyse the identifiers contained in the provided url over the different species and projects the result to Homo Sapiens",
            description = "The projection is calculated by the orthologous slot in the Reactome database. Use page and pageSize " +
                    "to reduce the amount of data retrieved. Use sortBy and order to sort the result by your preferred option. " +
                    "The resource field will filter the results to show only those corresponding to the preferred molecule type " +
                    "(TOTAL includes all the different molecules type)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')"),
            @ApiResponse(responseCode = "422", description = "The provided URL is not processable")})
    @RequestMapping(value = "/url/projection", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostURLToHuman(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>url</b> A URL pointing to the data to be analysed",
                    required = true
            )
            @RequestBody String url,
            @Parameter(name = "interactors", description = "Include interactors", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean interactors,
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
            HttpServletRequest request) {
        UserData ud = controller.getUserDataFromURL(url);
        String fileName = controller.getFileNameFromURL(url);
        return controller.analyse(ud, request, true, interactors, fileName, includeDisease)
                .filterPathways(resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @Operation(summary = "Analyse the identifiers contained in the provided url over the different species",
            description = "Use page and pageSize to reduce the amount of data retrieved. Use sortBy and order to sort the result by your " +
                    "preferred option. The resource field will filter the results to show only those corresponding to the preferred " +
                    "molecule type (TOTAL includes all the different molecules type)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')"),
            @ApiResponse(responseCode = "422", description = "The provided URL is not processable")})
    @RequestMapping(value = "/url", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostURL(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>url</b> A URL pointing to the data to be analysed",
                    required = true
            )
            @RequestBody String url,
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
            HttpServletRequest request) {
        UserData ud = controller.getUserDataFromURL(url);
        String fileName = controller.getFileNameFromURL(url);
        List<Species> speciesList = controller.getSpeciesList(species);
        return controller.analyse(ud, request, false, interactors, fileName, includeDisease)
                .filterPathways(speciesList, resource, pValue, includeDisease, min, max)
                .getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @Autowired
    public void setController(AnalysisHelper controller) {
        this.controller = controller;
    }
}
