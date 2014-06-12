package org.reactome.server.analysis.contoller;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.reactome.server.analysis.helper.AnalysisHelper;
import org.reactome.server.analysis.helper.DownloadHelper;
import org.reactome.server.analysis.result.AnalysisStoredResult;
import org.reactome.server.components.analysis.model.AnalysisIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(value = "Retrieve downloadable files in CSV format")
@RequestMapping(value = "/download")
public class DownloadController {
    
    @Autowired
    private AnalysisHelper controller;

    @ApiOperation(value = "Downloads all hit pathways for a given analysis", notes = "")
    @RequestMapping(value = "/{token}/pathways/{resource}/{filename}.csv", method = RequestMethod.GET , produces = "text/csv" )
    @ResponseBody
    public FileSystemResource downloadHitPathways( @ApiParam(name = "token", required = true, value = "The token associated with the data to download")
                                                  @PathVariable String token,
                                                   @ApiParam(name = "resource", value = "the resource to sort", required = true, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
                                                  @PathVariable String resource,
                                                   @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                  @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.controller.getFromToken(token);
        return DownloadHelper.getHitPathwaysCVS(filename, asr, resource);
    }

    @ApiOperation(value = "Downloads all entities for certain pathway for a given analysis", notes = "")
    @RequestMapping(value = "/{token}/entities/found/{resource}/{filename}.csv", method = RequestMethod.GET , produces = "text/csv" )
    @ResponseBody
    public FileSystemResource downloadMappingResult( @ApiParam(name = "token", required = true, value = "The token associated with the data to download")
                                                     @PathVariable String token,
                                                     @ApiParam(name = "resource", value = "the resource to sort", required = true, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
                                                     @PathVariable String resource,
                                                     @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                     @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.controller.getFromToken(token);
        return DownloadHelper.getIdentifiersFoundMappingCVS(filename, asr, resource);
    }

    @ApiOperation(value = "Downloads a list of the not found identifiers", notes = "")
    @RequestMapping(value = "/{token}/entities/notfound/{filename}.csv", method = RequestMethod.GET , produces = "text/csv" )
    @ResponseBody
    public FileSystemResource downloadNotFound( @ApiParam(name = "token", required = true, value = "The token associated with the data to download")
                                                @PathVariable String token,
                                                @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
                                                @PathVariable String filename) throws IOException {
        AnalysisStoredResult asr = this.controller.getFromToken(token);
        return DownloadHelper.getNotFoundIdentifiers(filename, asr);
    }

//    @ApiOperation(value = "Downloads all entities for certain pathway for a given analysis", notes = "")
//    @RequestMapping(value = "/{token}/entities/found/{resource}/{pathway}/{filename}.csv", method = RequestMethod.GET , produces = "text/csv" )
//    @ResponseBody
//    public FileSystemResource downloadPathwayResult( @ApiParam(name = "token", required = true, value = "The token associated with the data to download")
//                                                    @PathVariable String token,
//                                                     @ApiParam(name = "resource", value = "the resource to sort", required = true, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
//                                                    @PathVariable String resource,
//                                                     @ApiParam(name = "pathway", required = true, value = "The dbId of the pathway of interest")
//                                                    @PathVariable Long pathway,
//                                                     @ApiParam(name = "filename", value = "the file name for the downloaded information", required = true, defaultValue = "result")
//                                                    @PathVariable String filename) throws IOException {
//        AnalysisStoredResult asr = this.controller.getFromToken(token);
//        return DownloadHelper.getHitPathwaysCVS(filename, asr, resource);
//    }

}
