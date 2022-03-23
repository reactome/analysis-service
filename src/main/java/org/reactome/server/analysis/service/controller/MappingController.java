package org.reactome.server.analysis.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "mapping", description = "Identifiers mapping methods")
@RequestMapping(value = "/mapping")
public class MappingController {

    private AnalysisHelper controller;

    @Operation(summary = "Maps the post identifiers over the different species and projects the result to Homo Sapiens",
            description = "The projection is calculated by the orthologous slot in the Reactome database.")
    @ApiResponses({@ApiResponse(responseCode = "400", description = "Bad request")})
    @RequestMapping(value = "/projection", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingToHuman(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>input</b> Identifiers to be mapped (if expression values are submitted these will be omitted in the result)",
                    required = true
            )
            @RequestBody String input,
            @Parameter(name = "interactors", description = "Include interactors", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(input);
        return controller.getMapping(ud, true, interactors);
    }

    @Operation(description = "Maps the post identifiers over the different species")
    @ApiResponses({@ApiResponse(responseCode = " 400", description = "Bad request")})
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMapping(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>input</b> Identifiers to be mapped (if expression values åre submitted these will be omitted in the result)",
                    required = true
            )
            @RequestBody String input,
            @Parameter(name = "interactors", description = "Include interactors", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(input);
        return controller.getMapping(ud, false, interactors);
    }

    @Operation(summary = "Maps the identifiers in the file over the different species and projects the result to Homo Sapiens",
            description = "The projection is calculated by the orthologous slot in the Reactome database.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')")})
    @RequestMapping(value = "/form/projection", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostFileToHuman(@Parameter(name = "file", required = true, description = "A file with the data to be mapped")
                                                        @RequestPart MultipartFile file,
                                                        @Parameter(name = "interactors", description = "Include interactors", example = "false")
                                                        @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(file);
        return controller.getMapping(ud, true, interactors);
    }

    @Operation(description = "Maps the identifiers in the file over the different species")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')")})
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostFile(@Parameter(name = "file", required = true, description = "A file with the data to be mapped")
                                                 @RequestPart MultipartFile file,
                                                 @Parameter(name = "interactors", description = "Include interactors", example = "false")
                                                 @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserData(file);
        return controller.getMapping(ud, false, interactors);
    }

    @Operation(summary = "Maps the identifiers contained in the provided url over the different species and projects the result to Homo Sapiens",
            description = "The projection is calculated by the orthologous slot in the Reactome database.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')"),
            @ApiResponse(responseCode = "422", description = "The provided URL is not processable")})
    @RequestMapping(value = "/url/projection", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostURLToHuman(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "<b>url</b> A URL pointing to the data to be analysed",
                    required = true
            )
            @RequestBody String url,
            @Parameter(name = "interactors", description = "Include interactors", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserDataFromURL(url);
        return controller.getMapping(ud, true, interactors);
    }

    @Operation(summary = "Maps the identifiers contained in the provided url over the different species")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "413", description = "The file size is larger than the maximum configured size (50MB)"),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type (only 'text/plain')"),
            @ApiResponse(responseCode = "422", description = "The provided URL is not processable")})
    @RequestMapping(value = "/url", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public List<MappedEntity> getMappingPostURL(@Parameter(name = "url", required = true, description = "A URL pointing to the data to be mapped")
                                                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                        description = "<b>url</b> A URL pointing to the data to be mapped",
                                                        required = true
                                                )
                                                @RequestBody String url,
                                                @Parameter(name = "interactors", description = "Include interactors", example = "false")
                                                @RequestParam(required = false, defaultValue = "false") Boolean interactors) {
        UserData ud = controller.getUserDataFromURL(url);
        return controller.getMapping(ud, false, interactors);
    }

    @Autowired
    public void setController(AnalysisHelper controller) {
        this.controller = controller;
    }

}
