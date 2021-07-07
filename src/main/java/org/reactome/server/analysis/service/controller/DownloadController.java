package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.*;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.external.ExternalAnalysisResult;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.service.helper.DownloadHelper;
import org.reactome.server.graph.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(tags = {"download"})
@RequestMapping(value = "/download")
public class DownloadController {

    private TokenUtils token;
    private GeneralService generalService;

    @ApiOperation(value = "Downloads all hit pathways for a given analysis",
                  notes = "The results are filtered by the selected resource. The filename is the one to be suggested in the download window.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/pathways/{resource}/{filename}.csv", method = RequestMethod.GET, produces = "text/csv" )
    @ResponseBody
    public FileSystemResource downloadResultCSV( @ApiParam(name = "token", required = true, value = "The token associated with the data to download")
                                                @PathVariable String token,
                                                 @ApiParam(name = "resource", value = "the preferred resource", required = true, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                                @PathVariable String resource,
                                                 @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.token.getFromToken(token);
        return DownloadHelper.getHitPathwaysCVS(filename, asr, resource);
    }

    @ApiOperation(value = "Returns the complete result in json format",
                  notes = "The results are not filtered by any means. The json file contains the whole stored result based on chosen analysis options and submitted data.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/result.json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ExternalAnalysisResult downloadResultJSON( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                     @PathVariable String token) {
        AnalysisStoredResult result = this.token.getFromToken(token);
        return new ExternalAnalysisResult(result, generalService.getDBInfo().getVersion());
    }

    @ApiOperation(value = "Returns the complete result in json format (gzipped)",
            notes = "The results are not filtered by any means. The json file contains the whole stored result based on chosen analysis options and submitted data.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/result.json.gz", method = RequestMethod.GET, produces = {"application/x-gzip", "application/gzip"})
    @ResponseBody
    public FileSystemResource downloadResultGZIP(@ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                 @PathVariable String token,
                                                 HttpServletResponse response) throws IOException {
        response.setContentType("application/x-gzip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "result.json.gz" + "\"");
        AnalysisStoredResult result = this.token.getFromToken(token);
        final ExternalAnalysisResult er = new ExternalAnalysisResult(result, generalService.getDBInfo().getVersion());
        return DownloadHelper.getExternalResultsGZIP("result", er);
    }


    @ApiOperation(value = "Downloads found identifiers for a given analysis and resource",
                  notes = "The identifiers are filtered by the selected resource. The filename is the one to be suggested in the download window.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/entities/found/{resource}/{filename}.csv", method = RequestMethod.GET, produces = "text/csv" )
    @ResponseBody
    public FileSystemResource downloadMappingResult( @ApiParam(name = "token", required = true, value = "The token associated with the data to download")
                                                    @PathVariable String token,
                                                     @ApiParam(name = "resource", value = "the preferred resource", required = true, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                                    @PathVariable String resource,
                                                     @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                    @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.token.getFromToken(token);
        return DownloadHelper.getIdentifiersFoundMappingCVS(filename, asr, resource);
    }

    @ApiOperation(value = "Downloads a list of the not found identifiers",
                  notes = "Those identifiers from the user sample that are not present up to the current data version. The filename is the one to be suggested in the download window.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/entities/notfound/{filename}.csv", method = RequestMethod.GET, produces = "text/csv" )
    @ResponseBody
    public FileSystemResource downloadNotFound( @ApiParam(name = "token", required = true, value = "The token associated with the data to download")
                                               @PathVariable String token,
                                                @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
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
