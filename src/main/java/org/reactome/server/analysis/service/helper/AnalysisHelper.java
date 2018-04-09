package org.reactome.server.analysis.service.helper;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;
import org.reactome.server.analysis.core.exception.SpeciesNotFoundException;
import org.reactome.server.analysis.core.methods.EnrichmentAnalysis;
import org.reactome.server.analysis.core.methods.SpeciesComparison;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.parser.exception.ParserException;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.*;
import org.reactome.server.analysis.core.result.model.AnalysisSummary;
import org.reactome.server.analysis.core.result.report.AnalysisReport;
import org.reactome.server.analysis.core.result.report.ReportParameters;
import org.reactome.server.analysis.core.result.utils.ResultDataUtils;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.core.result.utils.Tokenizer;
import org.reactome.server.analysis.core.util.InputUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.net.ssl.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class AnalysisHelper {

    private static final Logger logger = LoggerFactory.getLogger("analysisLogger");

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private CommonsMultipartResolver multipartResolver;

    @Autowired
    private EnrichmentAnalysis enrichmentAnalysis;

    @Autowired
    private SpeciesComparison speciesComparison;

    public AnalysisStoredResult analyse(UserData userData, boolean toHuman, boolean includeInteractors){
        return analyse(userData, toHuman, includeInteractors, null);
    }

    public AnalysisStoredResult analyse(UserData userData, Boolean toHuman, Boolean includeInteractors, String userFileName){
        AnalysisType type =  userData.getExpressionColumnNames().isEmpty() ? AnalysisType.OVERREPRESENTATION : AnalysisType.EXPRESSION;
        ReportParameters reportParams = new ReportParameters(type, toHuman, includeInteractors);
        SpeciesNode speciesNode = toHuman ? SpeciesNodeFactory.getHumanNode() : null;
        if(Tokenizer.hasToken(userData.getInputMD5(), toHuman, includeInteractors)){
            String token = Tokenizer.getOrCreateToken(userData.getInputMD5(), toHuman, includeInteractors);
            AnalysisSummary summary = tokenUtils.getAnalysisSummary(token, toHuman, includeInteractors, userData.getSampleName(), type, userFileName);
            try {
                String fileName = tokenUtils.getFileName(token);
                return ResultDataUtils.getAnalysisResult(fileName, reportParams);
            } catch (FileNotFoundException e) {
                logger.trace("No TOKEN found. Analysing...");
                return analyse(summary, userData, speciesNode, includeInteractors, reportParams);
            } catch (Exception e){
                logger.warn("Cannot retrieve the result from the MD5 token. Analysing again...");
            }
        }
        String token = Tokenizer.getOrCreateToken(userData.getInputMD5(), toHuman, includeInteractors);
        AnalysisSummary summary = tokenUtils.getAnalysisSummary(token, toHuman, includeInteractors, userData.getSampleName(), type, userFileName);
        return analyse(summary, userData, speciesNode, includeInteractors, reportParams);
    }

    public AnalysisStoredResult compareSpecies(Long from, Long to){
        SpeciesNode speciesFrom = SpeciesNodeFactory.getSpeciesNode(from, "", "");
        SpeciesNode speciesTo = SpeciesNodeFactory.getSpeciesNode(to, "", "");

        ReportParameters reportParams = new ReportParameters(AnalysisType.SPECIES_COMPARISON);
        String fakeMD5 = tokenUtils.getFakedMD5(speciesFrom, speciesTo);
        boolean human = from.equals(SpeciesNodeFactory.getHumanNode().getSpeciesID());
        if(Tokenizer.hasToken(fakeMD5, human, false)){
            String token = Tokenizer.getOrCreateToken(fakeMD5, human, false);
            try {
                String fileName = tokenUtils.getFileName(token);
                return ResultDataUtils.getAnalysisResult(fileName, reportParams);
            } catch (FileNotFoundException e) {
                //Nothing here
            }
        }

        try {
            UserData ud = speciesComparison.getSyntheticUserData(speciesTo);
            String token = Tokenizer.getOrCreateToken(fakeMD5, human, false);
            AnalysisSummary summary = new AnalysisSummary(token, null, false,  null, AnalysisType.SPECIES_COMPARISON, to);
            return analyse(summary, ud, speciesFrom, false, reportParams);
        } catch (SpeciesNotFoundException e) {
            throw new ResourceNotFoundException();
        }

    }

    public <T> List<T> filter(List<T> list, Integer pageSize, Integer page){
        if(pageSize!=null && page!=null){
            pageSize = pageSize < 0 ? 0 : pageSize;
            page = page < 0 ? 0 : page;
            int from = pageSize * (page - 1);
            if(from < list.size() && from > -1){
                int to = from + pageSize;
                to = to > list.size() ? list.size() : to;
                return list.subList(from, to);
            }else{
                return new LinkedList<>();
            }
        }else{
            return list;
        }
    }

    public List<String> getInputIdentifiers(String input){
        List<String> rtn = new LinkedList<>();
        if(input.contains("=")) input = input.split("=")[1];
        for (String line : input.split("\n")) {
            if(line.isEmpty()) continue;
            for (String value : line.split(",")) {
                String id = value.trim();
                if (id.startsWith("R-")) {
                    rtn.add(id);
                } else {
                    try {
                        //Next one is to ensure that if it doesn't start with R- at least is a valid db_id (is Long)
                        rtn.add(Long.valueOf(id).toString());
                    } catch (NumberFormatException e) {
                        //Nothing here because
                        //throw new ResourceNotFoundException(); is not longer needed
                    }
                }
            }
        }
        return rtn;
    }

    public UserData getUserData(String input){
        try {
            return InputUtils.getUserData(input);
        } catch (IOException e) {
            throw new UnsupportedMediaTypeException();
        } catch (ParserException e) {
            throw new DataFormatException(e.getErrorMessages());
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public UserData getUserData(MultipartFile file){
        if(!file.isEmpty()){
            try {
                String mimeType = detectMimeType(TikaInputStream.get(file.getInputStream()));
                if(!isAcceptedContentType(mimeType)){
                    throw new UnsupportedMediaTypeException();
                }

                return InputUtils.getUserData(file.getInputStream());
            } catch (IOException e) {
                throw new UnsupportedMediaTypeException();
            } catch (ParserException e) {
                throw new DataFormatException(e.getErrorMessages());
            }
        }
        throw new UnsupportedMediaTypeException();
    }

    public UserData getUserDataFromURL(String url){
        if(url!=null && !url.isEmpty()) {
            InputStream is;
            try {
                HttpURLConnection conn;
                URL aux = new URL(url);
                if(aux.getProtocol().contains("https")){
                    doTrustToCertificates(); //accepting the certificate by default
                    HttpsURLConnection tmpConn = (HttpsURLConnection) aux.openConnection();
                    is = tmpConn.getInputStream();
                    conn = tmpConn;
                }else{
                    URLConnection tmpConn = aux.openConnection();
                    is = tmpConn.getInputStream();
                    conn = (HttpURLConnection) tmpConn;
                }

                if(conn.getContentLength() > multipartResolver.getFileUpload().getSizeMax()){
                    throw new RequestEntityTooLargeException();
                }
                if(!isAcceptedContentType(conn.getContentType())){
                    throw new UnsupportedMediaTypeException();
                }
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {

                throw new UnprocessableEntityException();
            }
            try {
                return InputUtils.getUserData(is);
            } catch (IOException e) {
                throw new UnsupportedMediaTypeException();
            } catch (ParserException e) {
                throw new DataFormatException(e.getErrorMessages());
            }
        }
        throw new UnsupportedMediaTypeException();
    }

    public String getFileNameFromURL(String url){
        String name = "";
        if(url!=null && !url.isEmpty()) {
            try {
                name = FilenameUtils.getName((new URL(url)).getFile());
            } catch (MalformedURLException e) {
                /*Nothing here*/
            }
        }
        return name;
    }

    private AnalysisStoredResult analyse(AnalysisSummary summary, UserData userData, SpeciesNode speciesNode, Boolean includeInteractors, ReportParameters reportParams){
        long start = System.currentTimeMillis();
        Set<AnalysisIdentifier> identifiers = userData.getIdentifiers();
        HierarchiesData resAux = this.enrichmentAnalysis.overRepresentation(identifiers, speciesNode, includeInteractors);

        AnalysisStoredResult result = new AnalysisStoredResult(userData, resAux);
        result.setSummary(summary);
        result.setHitPathways(resAux.getUniqueHitPathways(speciesNode));
        tokenUtils.saveResult(result);

        //Report
        reportParams.setAnalysisStoredResult(result);
        reportParams.setMilliseconds(System.currentTimeMillis() - start);
        AnalysisReport.reportNewAnalysis(reportParams);

        return result;
    }

    // trusting all certificate
    @SuppressWarnings("Duplicates")
    private void doTrustToCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {}

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = (urlHostName, session) -> {
            if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                logger.warn("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
            }
            return true;
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    /**
     * Detect MimeType using apache tika.
     * jMimeMagic has failed when analysing the PSIMITAB .txt file export from IntAct page
     *
     * @throws IOException if the document input stream could not be read
     */
    private String detectMimeType(TikaInputStream tikaInputStream) throws IOException {
        final Detector DETECTOR = new DefaultDetector(MimeTypes.getDefaultMimeTypes());

        try {
            return DETECTOR.detect(tikaInputStream, new Metadata()).toString();
        } finally {
            if (tikaInputStream != null) {
                tikaInputStream.close();
            }
        }
    }

    public boolean isAcceptedContentType(String contentType){
        return contentType == null || contentType.contains("text/plain");
    }
}
