package org.reactome.server.analysis.service.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.external.ExternalAnalysisResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.service.helper.DownloadHelper;
import org.reactome.server.graph.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Tag(name = "download", description = "Methods to download different views of a result")
@RequestMapping(value = "/download")
public class DownloadController {

    private TokenUtils token;
    private GeneralService generalService;

    @Operation(summary = "Downloads all hit pathways for a given analysis",
            description = "The results are filtered by the selected resource. The filename is the one to be suggested in the download window.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/pathways/{resource}/{filename}.csv", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody
    public FileSystemResource downloadResultCSV(@Parameter(name = "token", required = true, description = "The token associated with the data to download")
                                                @PathVariable String token,
                                                @Parameter(name = "resource", schema = @Schema(description = "the preferred resource", required = true, example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI", "PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                @PathVariable String resource,
                                                @Parameter(name = "filename", description = "the file name for the downloaded information", required = true, example = "result")
                                                @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.token.getFromToken(token);
        return DownloadHelper.getHitPathwaysCVS(filename, asr, resource);
    }

    @Operation(summary = "Returns the complete result in json format",
            description = "The results are not filtered by any means. The json file contains the whole stored result based on chosen analysis options and submitted data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/result.json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ExternalAnalysisResult downloadResultJSON(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                     @PathVariable String token,
                                                     @Parameter(name = "importableOnly", description = "Only include resources which can be later imported", example = "false")
                                                     @RequestParam(required = false, defaultValue = "false") Boolean importableOnly) {
        AnalysisStoredResult result = this.token.getFromToken(token);
        return new ExternalAnalysisResult(result, generalService.getDBInfo().getVersion(), importableOnly);
    }

    @Operation(summary = "Returns the complete result in json format (gzipped)",
            description = "The results are not filtered by any means. The json file contains the whole stored result based on chosen analysis options and submitted data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/result.json.gz", method = RequestMethod.GET, produces = {"application/x-gzip", "application/gzip"})
    @ResponseBody
    public FileSystemResource downloadResultGZIP(@Parameter(name = "token", required = true, description = "The token associated with the data to query")
                                                 @PathVariable String token,
                                                 HttpServletResponse response) throws IOException {
        response.setContentType("application/x-gzip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "result.json.gz" + "\"");
        AnalysisStoredResult result = this.token.getFromToken(token);
        final ExternalAnalysisResult er = new ExternalAnalysisResult(result, generalService.getDBInfo().getVersion());
        return DownloadHelper.getExternalResultsGZIP("result", er);
    }


    @Operation(summary = "Downloads found identifiers for a given analysis and resource",
            description = "The identifiers are filtered by the selected resource. The filename is the one to be suggested in the download window.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/entities/found/{resource}/{filename}.csv", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody
    public FileSystemResource downloadMappingResult(@Parameter(name = "token", required = true, description = "The token associated with the data to download")
                                                    @PathVariable String token,
                                                    @Parameter(name = "resource", schema = @Schema(description = "the preferred resource", required = true, example = "TOTAL", allowableValues = {"TOTAL", "UNIPROT", "ENSEMBL", "CHEBI", "IUPHAR", "MIRBASE", "NCBI_PROTEIN", "EMBL", "COMPOUND", "PUBCHEM_COMPOUND"}))
                                                    @PathVariable String resource,
                                                    @Parameter(name = "filename", description = "the file name for the downloaded information", required = true, example = "result")
                                                    @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.token.getFromToken(token);
        return DownloadHelper.getIdentifiersFoundMappingCVS(filename, asr, resource);
    }

    @Operation(summary = "Downloads a list of the not found identifiers",
            description = "Those identifiers from the user sample that are not present up to the current data version. The filename is the one to be suggested in the download window.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "No result corresponding to the token was found"),
            @ApiResponse(responseCode = "410", description = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/entities/notfound/{filename}.csv", method = RequestMethod.GET, produces = "text/csv")
    @ResponseBody
    public FileSystemResource downloadNotFound(@Parameter(name = "token", required = true, description = "The token associated with the data to download")
                                               @PathVariable String token,
                                               @Parameter(name = "filename", description = "the file name for the downloaded information", required = true, example = "result")
                                               @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.token.getFromToken(token);
        return DownloadHelper.getNotFoundIdentifiers(filename, asr);
    }

    @Autowired
    public void setToken(TokenUtils token) {
        this.token = token;
    }

    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

}
