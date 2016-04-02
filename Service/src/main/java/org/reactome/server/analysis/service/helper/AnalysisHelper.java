package org.reactome.server.analysis.service.helper;

import net.sf.jmimemagic.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.reactome.server.analysis.core.components.EnrichmentAnalysis;
import org.reactome.server.analysis.core.components.SpeciesComparison;
import org.reactome.server.analysis.core.exception.SpeciesNotFoundException;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.util.InputUtils;
import org.reactome.server.analysis.parser.exception.ParserException;
import org.reactome.server.analysis.service.exception.*;
import org.reactome.server.analysis.service.model.AnalysisSummary;
import org.reactome.server.analysis.service.report.AnalysisReport;
import org.reactome.server.analysis.service.report.ReportParameters;
import org.reactome.server.analysis.service.result.AnalysisStoredResult;
import org.reactome.server.analysis.service.utils.ResultDataUtils;
import org.reactome.server.analysis.service.utils.Tokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
@Scope("singleton")
public class AnalysisHelper {

    public enum Type {
        SPECIES_COMPARISON,
        OVERREPRESENTATION,
        EXPRESSION;

        public static Type getType(String type){
            for (Type t : values()) {
                if(t.toString().toLowerCase().equals(type.toLowerCase())){
                    return t;
                }
            }
            return null;
        }
    }

    private static Logger logger = Logger.getLogger(AnalysisHelper.class.getName());

    private String pathDirectory;

    @Autowired
    CommonsMultipartResolver multipartResolver;

    @Autowired
    private EnrichmentAnalysis enrichmentAnalysis;

    @Autowired
    private SpeciesComparison speciesComparison;

    public AnalysisStoredResult analyse(UserData userData, boolean toHuman, boolean includeInteractors){
        return analyse(userData, toHuman, includeInteractors, null);
    }

    public AnalysisStoredResult analyse(UserData userData, Boolean toHuman, Boolean includeInteractors, String userFileName){
        Type type =  userData.getExpressionColumnNames().isEmpty() ? Type.OVERREPRESENTATION : Type.EXPRESSION;
        ReportParameters reportParams = new ReportParameters(type, toHuman, includeInteractors);
        SpeciesNode speciesNode = toHuman ? SpeciesNodeFactory.getHumanNode() : null;
        if(Tokenizer.hasToken(userData.getInputMD5(), toHuman, includeInteractors)){
            String token = Tokenizer.getOrCreateToken(userData.getInputMD5(), toHuman, includeInteractors);
            AnalysisSummary summary = getAnalysisSummary(token, toHuman, includeInteractors, userData.getSampleName(), type, userFileName);
            try {
                String fileName = getFileName(token);
                return ResultDataUtils.getAnalysisResult(fileName, reportParams);
            } catch (FileNotFoundException e) {
                logger.trace("No TOKEN found. Analysing...");
                return analyse(summary, userData, speciesNode, includeInteractors, reportParams);
            } catch (Exception e){
                logger.error("Error retrieving the result from the MD5 token. Analysing again...");
            }
        }
        String token = Tokenizer.getOrCreateToken(userData.getInputMD5(), toHuman, includeInteractors);
        AnalysisSummary summary = getAnalysisSummary(token, toHuman, includeInteractors, userData.getSampleName(), type, userFileName);
        return analyse(summary, userData, speciesNode, includeInteractors, reportParams);
    }

    public AnalysisStoredResult compareSpecies(Long from, Long to){
        boolean includeInteractors = false;
        SpeciesNode speciesFrom = SpeciesNodeFactory.getSpeciesNode(from, "");
        SpeciesNode speciesTo = SpeciesNodeFactory.getSpeciesNode(to, "");

        ReportParameters reportParams = new ReportParameters(Type.SPECIES_COMPARISON);
        String fakeMD5 = this.getFakedMD5(speciesFrom, speciesTo);
        boolean human = from.equals(SpeciesNodeFactory.getHumanNode().getSpeciesID());
        if(Tokenizer.hasToken(fakeMD5, human, false)){
            String token = Tokenizer.getOrCreateToken(fakeMD5, human, false);
            try {
                String fileName = getFileName(token);
                return ResultDataUtils.getAnalysisResult(fileName, reportParams);
            } catch (FileNotFoundException e) {
                //Nothing here
            }
        }

        try {
            UserData ud = speciesComparison.getSyntheticUserData(speciesTo);
            String token = Tokenizer.getOrCreateToken(fakeMD5, human, false);
            AnalysisSummary summary = new AnalysisSummary(token, null, includeInteractors,  null, Type.SPECIES_COMPARISON, to);
            return analyse(summary, ud, speciesFrom, false, reportParams);
        } catch (SpeciesNotFoundException e) {
            throw new ResourceNotFoundException();
        }

    }

    private AnalysisSummary getAnalysisSummary(String token, Boolean projection, Boolean interactors, String sampleName, Type type, String userFileName){
        if(userFileName!=null && !userFileName.isEmpty()){
            return new AnalysisSummary(token, projection, interactors, sampleName, type, userFileName);
        }else{
            return new AnalysisSummary(token, projection, interactors, sampleName, type, true);
        }
    }

    private String getFakedMD5(SpeciesNode speciesFrom, SpeciesNode speciesTo){
        return Type.SPECIES_COMPARISON.toString() + speciesFrom.getSpeciesID() + "-" + speciesTo.getSpeciesID();
    }

    public AnalysisStoredResult getFromToken(String token) {
        String fileName = getFileName(token);
        if(fileName!=null){
            try {
                return ResultDataUtils.getAnalysisResult(fileName);
            } catch (FileNotFoundException e) {
                //should be alive is only true when the token follows the rule and the resulting date is in the last 7 days
                if(Tokenizer.shouldBeAlive(token)){
                    throw new ResourceGoneException();
                }
            }
        }
        throw new ResourceNotFoundException();
    }

    public List<Long> getInputIds(String input){
        List<Long> rtn = new LinkedList<Long>();
        if(input.contains("=")) input = input.split("=")[1];
        for (String line : input.split("\n")) {
            if(line.isEmpty()) continue;
            for (String value : line.split(",")) {
                try{
                    rtn.add(Long.valueOf(value.trim()));
                }catch (NumberFormatException e){
                    throw new ResourceNotFoundException();
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
                try {
                    //MagicMatch tries to get the content type of the original file
                    //sent via an HTML multi-part form (remember that the file mime-type
                    //is always 'application/octet-stream' when using the form)
                    MagicMatch match = Magic.getMagicMatch(file.getBytes());
                    if(!isAcceptedContentType(match.getMimeType())){
                        throw new UnsupportedMediaTypeException();
                    }
                } catch (MagicMatchNotFoundException e) {
                    logger.error(e.getMessage(),e);
                    e.printStackTrace();
                    throw new UnsupportedMediaTypeException();
                } catch (MagicException e) {
                    logger.error(e.getMessage(),e);
                    throw new UnsupportedMediaTypeException();
                } catch (MagicParseException e) {
                    logger.error(e.getMessage(),e);
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

    public void setPathDirectory(String pathDirectory) {
        this.pathDirectory = pathDirectory;
    }

    private AnalysisStoredResult analyse(AnalysisSummary summary, UserData userData, SpeciesNode speciesNode, Boolean includeInteractors, ReportParameters reportParams){
        long start = System.currentTimeMillis();
        Set<AnalysisIdentifier> identifiers = userData.getIdentifiers();
        HierarchiesData resAux = this.enrichmentAnalysis.overRepresentation(identifiers, speciesNode, includeInteractors);

        AnalysisStoredResult result = new AnalysisStoredResult(userData, resAux);
        result.setSummary(summary);
        result.setHitPathways(resAux.getUniqueHitPathways(speciesNode));
        this.saveResult(result);

        //Report
        reportParams.setAnalysisStoredResult(result);
        reportParams.setMilliseconds(System.currentTimeMillis() - start);
        AnalysisReport.reportNewAnalysis(reportParams);

        return result;
    }

    private String getFileName(String token){
        String name = Tokenizer.getName(token);
        return String.format("%s/res_%s.bin", this.pathDirectory, name);
    }

    private void saveResult(final AnalysisStoredResult result){
        String fileName = getFileName(result.getSummary().getToken());
        ResultDataUtils.kryoSerialisation(result, fileName);
    }

    private boolean isAcceptedContentType(String contentType){
        return contentType.contains("text/plain");
    }

    // trusting all certificate
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
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    logger.warn("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
}
