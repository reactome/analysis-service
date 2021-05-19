package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.*;
import org.reactome.server.analysis.core.model.UserData;
import org.reactome.server.analysis.core.result.model.MappedEntity;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(tags = {"mapping"})
@RequestMapping(value = "/mapping")
public class MappingController {

    private AnalysisHelper controller;

    @ApiOperation(value = "Maps the post identifiers over the different species and projects the result to Homo Sapiens",
                  notes = "The projection is calculated by the orthologous slot in the Reactome database.")
    @ApiResponses({@ApiResponse( code = 400, message = "Bad request" )})
    @RequestMapping(value = "/projection", method = RequestMethod.POST, consumes = "text/plain",  produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingToHuman(@ApiParam(name = "input", required = true, value = "Identifiers to be mapped (if expression values are submitted these will be omitted in the result)")
                                            @RequestBody String input,
                                                @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                            @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(input);
        return controller.getMapping(ud,  true, interactors);
    }

    @ApiOperation(value = "Maps the post identifiers over the different species")
    @ApiResponses({@ApiResponse( code = 400, message = "Bad request" )})
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMapping(@ApiParam(name = "input", required = true, value = "Identifiers to be mapped (if expression values åre submitted these will be omitted in the result)")
                                     @RequestBody String input,
                                    @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                     @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(input);
        return controller.getMapping(ud,  false, interactors);
    }

    @ApiOperation(value = "Maps the identifiers in the file over the different species and projects the result to Homo Sapiens",
                  notes = "The projection is calculated by the orthologous slot in the Reactome database.")
    @ApiResponses({
            @ApiResponse( code = 400, message = "Bad request" ),
            @ApiResponse( code = 413, message = "The file size is larger than the maximum configured size (50MB)"  ),
            @ApiResponse( code = 415, message = "Unsupported Media Type (only 'text/plain')" )})
    @RequestMapping(value = "/form/projection", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostFileToHuman(@ApiParam(name = "file", required = true, value = "A file with the data to be mapped")
                                                    @RequestPart MultipartFile file,
                                                   @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                                    @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(file);
        return controller.getMapping(ud,  true, interactors);
    }

    @ApiOperation(value = "Maps the identifiers in the file over the different species")
    @ApiResponses({
            @ApiResponse( code = 400, message = "Bad request" ),
            @ApiResponse( code = 413, message = "The file size is larger than the maximum configured size (50MB)"  ),
            @ApiResponse( code = 415, message = "Unsupported Media Type (only 'text/plain')" )})
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostFile(@ApiParam(name = "file", required = true, value = "A file with the data to be mapped")
                                             @RequestPart MultipartFile file,
                                            @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                             @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(file);
        return controller.getMapping(ud,  false, interactors);
    }

    @ApiOperation(value = "Maps the identifiers contained in the provided url over the different species and projects the result to Homo Sapiens",
                  notes = "The projection is calculated by the orthologous slot in the Reactome database.")
    @ApiResponses({
            @ApiResponse( code = 400, message = "Bad request" ),
            @ApiResponse( code = 413, message = "The file size is larger than the maximum configured size (50MB)"  ),
            @ApiResponse( code = 415, message = "Unsupported Media Type (only 'text/plain')" ),
            @ApiResponse( code = 422, message = "The provided URL is not processable" )})
    @RequestMapping(value = "/url/projection", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostURLToHuman(@ApiParam(name = "url", required = true, value = "A URL pointing to the data to be analysed")
                                                   @RequestBody String url,
                                                  @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                                   @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserDataFromURL(url);
        return controller.getMapping(ud,  true, interactors);
    }

    @ApiOperation(value = "Maps the identifiers contained in the provided url over the different species")
    @ApiResponses({
            @ApiResponse( code = 400, message = "Bad request" ),
            @ApiResponse( code = 413, message = "The file size is larger than the maximum configured size (50MB)"  ),
            @ApiResponse( code = 415, message = "Unsupported Media Type (only 'text/plain')" ),
            @ApiResponse( code = 422, message = "The provided URL is not processable" )})
    @RequestMapping(value = "/url", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostURL(@ApiParam(name = "url", required = true, value = "A URL pointing to the data to be mapped")
                                            @RequestBody String url,
                                           @ApiParam(name = "interactors", value = "Include interactors", defaultValue = "false")
                                            @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserDataFromURL(url);
        return controller.getMapping(ud,  false, interactors);
    }

    @Autowired
    public void setController(AnalysisHelper controller) {
        this.controller = controller;
    }

}
