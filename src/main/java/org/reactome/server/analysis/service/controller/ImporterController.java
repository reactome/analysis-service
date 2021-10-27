package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.*;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.model.AnalysisSummary;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(tags = {"import"})
@RequestMapping(value = "/import")
public class ImporterController {

    private AnalysisHelper controller;

    @ApiOperation(value = "Imports the posted json into the service",
                  notes = "The accepted format is the same as provided by the method <a href='#/download/downloadResultJSONUsingGET'>/#/download/{token}/result.json</a>.")
    @ApiResponses({
            @ApiResponse( code = 400, message = "Bad request. See more details in the response body"),
            @ApiResponse( code = 415, message = "Unsupported Media Type (only 'text/plain' or 'application/json')")})
    @RequestMapping(value = "/", method = RequestMethod.POST,
                    consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE, "application/gzip", "application/zip"},
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AnalysisSummary getPostText( @ApiParam(name = "input", required = true, value = "Identifiers to analyse followed by their expression (when applies)")
                                       @RequestBody String input,
                                       HttpServletRequest request) {
        //InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        AnalysisStoredResult asr = controller.getAnalysisStoredResult(input, request);
        return asr.getSummary();
    }

    @ApiOperation(value = "Imports the posted json file into the service",
                  notes = "The accepted format is the same as provided by the method <a href='#/download/downloadResultJSONUsingGET'>/#/download/{token}/result.json</a>. " +
                          "Note: The submitted file can be gzipped.")
    @ApiResponses({
            @ApiResponse( code = 400, message = "Bad request. See more details in the response body" ),
            @ApiResponse( code = 413, message = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse( code = 415, message = "Unsupported Media Type (only 'text/plain' or 'application/json')")})
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public AnalysisSummary getPostFile( @ApiParam(name = "file", required = true, value = "A file with the data to be analysed")
                                       @RequestPart MultipartFile file,
                                       HttpServletRequest request) {
        AnalysisStoredResult asr = controller.getAnalysisStoredResult(file, request);
        return asr.getSummary();
    }

    @ApiOperation(value = "Imports the json file provided by the posted url into the service",
                  notes = "The accepted format is the same as provided by the method <a href='#/download/downloadResultJSONUsingGET'>/#/download/{token}/result.json</a>. " +
                          "Note: The provided file can be gzipped.")
    @ApiResponses({
            @ApiResponse( code = 400, message = "Bad request. See more details in the response body" ),
            @ApiResponse( code = 413, message = "The file size is larger than the maximum configured size (50MB)"  ),
            @ApiResponse( code = 415, message = "Unsupported Media Type (only 'text/plain')" ),
            @ApiResponse( code = 422, message = "The provided URL is not processable" )})
    @RequestMapping(value = "/url", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public AnalysisSummary getPostURL( @ApiParam(name = "url", required = true, value = "A URL pointing to the data to be analysed")
                                      @RequestBody String url,
                                      HttpServletRequest request) {
        AnalysisStoredResult asr = controller.getAnalysisResultFromURL(url, request);
        return asr.getSummary();
    }

    @Autowired
    public void setController(AnalysisHelper controller) {
        this.controller = controller;
    }

}
