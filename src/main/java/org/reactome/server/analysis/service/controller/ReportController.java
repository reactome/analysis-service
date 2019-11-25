package org.reactome.server.analysis.service.controller;

import com.itextpdf.kernel.PdfException;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.ResourceNotFoundException;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.core.util.FormatUtils;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.analysis.report.AnalysisReport;
import org.reactome.server.tools.analysis.report.exception.AnalysisExporterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(tags = "report", description = "Retrieves report files in PDF format", position = 5)
@RequestMapping(value = "/report")
public class ReportController {

    private static final Object REPORT_SEMAPHORE = new Object();
    private static long REPORT_COUNT = 0L;
    private static final int ALLOWED_CONCURRENT_REPORTS = 2;

    @Value("${report.user:default}")
    private String reportUser;
    @Value("${report.password:default}")
    private String reportPassword;

    private static final Logger logger = LoggerFactory.getLogger("analysisReport");

    private AnalysisReport analysisReport;
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
    public void generatePdfReport( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                  @PathVariable String token,
                                   @ApiParam(name = "species", required = true, defaultValue = "Homo sapiens", value = "The species for which results will be reported")
                                  @PathVariable String species,
                                   @SuppressWarnings("unused")
                                   @ApiParam(name = "filename", required = true, defaultValue = "report", value = "The name of the file to be downloaded")
                                  @PathVariable String filename,
                                   @ApiParam(name = "number", value = "Number of pathways reported (max 50)", defaultValue = "25")
                                  @RequestParam(required = false, defaultValue = "25") Integer number,
                                   @ApiParam(name = "resource", value = "the resource to sort", defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,IUPHAR,MIRBASE,NCBI_PROTEIN,EMBL,COMPOUND,PUBCHEM_COMPOUND")
                                  @RequestParam(required = false, defaultValue = "TOTAL") String resource,
                                   @ApiParam(value = "Diagram Color Profile", defaultValue = "Modern", allowableValues = "Modern, Standard")
                                  @RequestParam(value = "diagramProfile", defaultValue = "Modern", required = false) String diagramProfile,
                                   @ApiParam(value = "Analysis  Color Profile", defaultValue = "Standard", allowableValues = "Standard, Strosobar, Copper Plus")
                                  @RequestParam(value = "analysisProfile", defaultValue = "Standard", required = false) String analysisProfile,
                                   @ApiParam(value = "Diagram Color Profile", defaultValue = "Barium Lithium", allowableValues = "Cooper, Cooper Plus, Barium Lithium, Calcium Salts")
                                  @RequestParam(value = "fireworksProfile", defaultValue = "Barium Lithium", required = false) String fireworksProfile,
                                  HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        long waitingTime = 0L;
        synchronized (REPORT_SEMAPHORE) {
            if (++REPORT_COUNT > ALLOWED_CONCURRENT_REPORTS) {
                long waitStart = System.currentTimeMillis();
                REPORT_SEMAPHORE.wait();
                waitingTime = System.currentTimeMillis() - waitStart;
            }
        }
        try {
            Species s = ReactomeGraphCore.getService(SpeciesService.class).getSpecies(species);
            if (s == null) throw new ResourceNotFoundException();

            number = Math.min(number, 50);
            number = Math.max(number, 1);

            long reportStart = System.currentTimeMillis();
            AnalysisStoredResult asr = this.token.getFromToken(token);

            response.addHeader("Content-Type", "application/pdf");
            OutputStream os = response.getOutputStream();
            analysisReport.create(asr, resource, s.getDbId(), number, diagramProfile, analysisProfile, fireworksProfile, os);

            Long reportTime = System.currentTimeMillis() - reportStart;
            logger.debug(String.format("_REPORT_ format:PDF token:%s pathways:%d time:%s", token, number, FormatUtils.getTimeFormatted(reportTime)));

            Map<String, String> map = getReportInformation(request);
            doAsyncSearchReport(map.get("ip-address"), waitingTime, reportTime, number, map.get("user-agent"));
        } catch (PdfException | IllegalStateException | IOException ise) {
            logger.debug(String.format("_REPORT_ format:PDF token:%s User_Closed_Connection", token));
        } catch (AnalysisExporterException  e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            synchronized (REPORT_SEMAPHORE) {
                REPORT_COUNT--;
                REPORT_SEMAPHORE.notify();
            }
        }
    }

    @Autowired
    public void setAnalysisReport(AnalysisReport analysisReport) {
        this.analysisReport = analysisReport;
    }

    @Autowired
    public void setToken(TokenUtils token) {
        this.token = token;
    }


    private void doAsyncSearchReport(String ip, Long waitingTime, Long reportTime, Integer pages, String userAgent) {
        new Thread(() -> report(ip, waitingTime, reportTime, pages, userAgent), "AnalysisPDFWaitingReportThread").start();
    }

    private void report(String ip, Long waitingTime, Long reportTime, Integer pages, String userAgent) {
        try {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(reportUser, reportPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
            URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/report/analysis/pdf/waiting");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("ip", ip));
            params.add(new BasicNameValuePair("waitingTime", String.valueOf(waitingTime)));
            params.add(new BasicNameValuePair("reportTime", String.valueOf(reportTime)));
            params.add(new BasicNameValuePair("pages", String.valueOf(pages)));
            params.add(new BasicNameValuePair("agent", userAgent));
            uriBuilder.addParameters(params);

            HttpGet httpGet = new HttpGet(uriBuilder.toString());
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("[REP001] The url {} returned the code {} and the report hasn't been created.", uriBuilder.toString(), statusCode);
            }
            client.close();
        } catch (ConnectException e) {
            logger.error("[REP002] Report service is unavailable");
        } catch (IOException | URISyntaxException e) {
            logger.error("[REP003] An unexpected error has occurred when saving a report");
        }
    }

    /**
     * Extra information to be sent to report service in order to store potential target
     */
    private Map<String, String> getReportInformation(HttpServletRequest request) {
        if (request == null) return null;

        Map<String, String> result = new HashMap<>();
        result.put("user-agent", request.getHeader("User-Agent"));
        String remoteAddr = request.getHeader("X-FORWARDED-FOR"); // Client IP
        if (!StringUtils.isEmpty(remoteAddr)) {
            // The general format of the field is: X-Forwarded-For: client, proxy1, proxy2 ... we only want the client
            remoteAddr = new StringTokenizer(remoteAddr, ",").nextToken().trim();
        } else {
            remoteAddr = request.getRemoteAddr();
        }
        result.put("ip-address", remoteAddr);
        return result;
    }

}
