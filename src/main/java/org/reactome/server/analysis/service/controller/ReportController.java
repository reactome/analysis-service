package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.*;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.core.util.FormatUtils;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.analysis.report.AnalysisReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(tags = "report", description = "Retrieves report files in PDF format", position = 4)
@RequestMapping(value = "/report")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger("analysisReport");

    @Autowired
    private AnalysisReport analysisReport;

    @Autowired
    private TokenUtils token;

    @ApiOperation(value = "Downloads a report for a given pathway analysis result",
                  notes = "This method provides a report for a given pathway analysis result in a PDF document. " +
                          "This document contains data about the analysis itself followed by the pathways overview and " +
                          "the most significant pathways overlaid with the analysis result. Users can download and store " +
                          "this information in a convenient format to be checked in the future when the 'token' is not " +
                          "longer available.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No result corresponding to the token was found"),
            @ApiResponse(code = 410, message = "Result deleted due to a new data release")})
    @RequestMapping(value = "/{token}/{species}/{filename}.pdf", method = RequestMethod.GET, produces = "application/pdf" )
    @ResponseBody
    public synchronized void generatePdfReport(@ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                    @PathVariable String token,
                                  @ApiParam(name = "species", required = true, defaultValue = "Homo sapiens", value = "The species for which results will be reported")
                                    @PathVariable String species,
                                  @ApiParam(name = "filename", required = true, defaultValue = "report", value = "The name of the file to be downloaded")
                                      @SuppressWarnings("unused")  @PathVariable String filename,
                                  @ApiParam(name = "number", value = "Number of pathways reported (max 50)", defaultValue = "25")
                                    @RequestParam(required = false, defaultValue = "25") Integer number,
                                  @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND")
                                    @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                  @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                                    @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,
                                  @ApiParam(value = "Analysis  Color Profile", defaultValue = "Standard", allowableValues = "Standard, Strosobar, Copper Plus")
                                    @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,
                                  @ApiParam(value = "Diagram Color Profile", defaultValue = "Barium Lithium", allowableValues = "Cooper, Cooper Plus, Barium Lithium, Calcium Salts")
                                    @RequestParam(value = "fireworksProfile", defaultValue = "Barium Lithium", required = false) String fireworksProfile,
                                  HttpServletResponse response) {
        Long start = System.currentTimeMillis();
        Species s = ReactomeGraphCore.getService(SpeciesService.class).getSpecies(species);
        if(s == null) throw new ResourceNotFoundException();

        number = Math.min(number, 50);
        number = Math.max(number, 1);

        AnalysisStoredResult asr = this.token.getFromToken(token);
        try {
            response.addHeader("Content-Type", "application/pdf");
            OutputStream os = response.getOutputStream();
            analysisReport.create(asr, resource, s.getDbId(), number, diagramProfile, analysisProfile, fireworksProfile, os);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            String time = FormatUtils.getTimeFormatted(System.currentTimeMillis() - start);
            logger.info(String.format("_REPORT_ format:PDF token:%s pathways:%d time:%s", token, number, time));
        }
    }
}
