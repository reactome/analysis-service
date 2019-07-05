package org.reactome.server.analysis.service.helper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;
import org.reactome.server.analysis.core.exception.SpeciesNotFoundException;
import org.reactome.server.analysis.core.methods.EnrichmentAnalysis;
import org.reactome.server.analysis.core.methods.IdentifiersMapping;
import org.reactome.server.analysis.core.methods.SpeciesComparison;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.parser.exception.ParserException;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.exception.*;
import org.reactome.server.analysis.core.result.external.ExternalAnalysisResult;
import org.reactome.server.analysis.core.result.model.AnalysisSummary;
import org.reactome.server.analysis.core.result.model.MappedEntity;
import org.reactome.server.analysis.core.result.report.AnalysisReport;
import org.reactome.server.analysis.core.result.report.ReportParameters;
import org.reactome.server.analysis.core.result.utils.ExternalAnalysisResultCheck;
import org.reactome.server.analysis.core.result.utils.ResultDataUtils;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.core.result.utils.Tokenizer;
import org.reactome.server.analysis.core.util.InputUtils;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.SpeciesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class AnalysisHelper {

    private static final Logger logger = LoggerFactory.getLogger("analysisLogger");

    private TokenUtils tokenUtils;
    private CommonsMultipartResolver multipartResolver;
    private EnrichmentAnalysis enrichmentAnalysis;
    private IdentifiersMapping identifiersMapping;
    private SpeciesComparison speciesComparison;
    private SpeciesService speciesService;
    private ExternalAnalysisResultCheck externalAnalysisResultCheck;

    @Autowired
    public void setTokenUtils(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    @Autowired
    public void setMultipartResolver(CommonsMultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Autowired
    public void setEnrichmentAnalysis(EnrichmentAnalysis enrichmentAnalysis) {
        this.enrichmentAnalysis = enrichmentAnalysis;
    }

    @Autowired
    public void setIdentifiersMapping(IdentifiersMapping identifiersMapping) {
        this.identifiersMapping = identifiersMapping;
    }

    @Autowired
    public void setSpeciesComparison(SpeciesComparison speciesComparison) {
        this.speciesComparison = speciesComparison;
    }

    @Autowired
    public void setSpeciesService(SpeciesService speciesService) {
        this.speciesService = speciesService;
    }

    @Autowired
    public void setExternalAnalysisResultCheck(ExternalAnalysisResultCheck externalAnalysisResultCheck) {
        this.externalAnalysisResultCheck = externalAnalysisResultCheck;
    }

    public AnalysisStoredResult analyse(UserData userData, HttpServletRequest request, boolean toHuman, boolean includeInteractors){
        return analyse(userData, request, toHuman, includeInteractors, null);
    }

    public AnalysisStoredResult analyse(UserData userData, HttpServletRequest request, Boolean toHuman, Boolean includeInteractors, String userFileName){
        AnalysisType type =  userData.getExpressionColumnNames().isEmpty() ? AnalysisType.OVERREPRESENTATION : AnalysisType.EXPRESSION;
        ReportParameters reportParams = new ReportParameters(type, toHuman, includeInteractors);
        SpeciesNode speciesNode = toHuman ? SpeciesNodeFactory.getHumanNode() : null;
        if(Tokenizer.hasToken(userData.getInputMD5(), toHuman, includeInteractors)){
            String token = Tokenizer.getOrCreateToken(userData.getInputMD5(), toHuman, includeInteractors);
            AnalysisSummary summary = tokenUtils.getAnalysisSummary(token, toHuman, includeInteractors, userData.getSampleName(), type, userFileName, getServerName(request));
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
        AnalysisSummary summary = tokenUtils.getAnalysisSummary(token, toHuman, includeInteractors, userData.getSampleName(), type, userFileName, getServerName(request));
        return analyse(summary, userData, speciesNode, includeInteractors, reportParams);
    }

    public AnalysisStoredResult compareSpecies(Long from, Long to, HttpServletRequest request){
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
            AnalysisSummary summary = new AnalysisSummary(token, null, false,  null, AnalysisType.SPECIES_COMPARISON, to, getServerName(request));
            return analyse(summary, ud, speciesFrom, false, reportParams);
        } catch (SpeciesNotFoundException e) {
            throw new ResourceNotFoundException();
        }

    }

    public List<MappedEntity> getMapping(UserData userData, boolean toHuman, boolean includeInteractors) {
        Set<String> identifiers = new HashSet<>();
        for (AnalysisIdentifier identifier : userData.getIdentifiers()) {
            identifiers.add(identifier.getId());
        }
        SpeciesNode speciesNode = toHuman ? SpeciesNodeFactory.getHumanNode() : null;
        return identifiersMapping.run(identifiers, speciesNode, includeInteractors);
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

    public UserData getUserData(MultipartFile file){
        if(!file.isEmpty()){
            try {
                String mimeType = detectMimeType(TikaInputStream.get(file.getInputStream()));
                if(!isAcceptedContentType(mimeType, "text/plain")){
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
            try {
                //return InputUtils.getUserData(getUrlInputStream(url, "text/plain"));
                return InputUtils.getUserData(getUrlInputStream(url));
            } catch (IOException e) {
                throw new UnsupportedMediaTypeException();
            } catch (ParserException e) {
                throw new DataFormatException(e.getErrorMessages());
            }
        }
        throw new UnsupportedMediaTypeException();
    }

    public AnalysisStoredResult getAnalysisStoredResult(String json, HttpServletRequest request) {
        try {
            ExternalAnalysisResult result = InputUtils.getExternalAnalysisResult(json);
            List<String> messages = externalAnalysisResultCheck.isValid(result);
            if (messages.isEmpty()) {
                String md5 = DigestUtils.md5DigestAsHex(json.getBytes());
                String newToken = Tokenizer.getOrCreateToken(md5, false, false);
                result.getSummary().setServer(getServerName(request));
                final AnalysisStoredResult analysisStoredResult = new AnalysisStoredResult(newToken, result);
                tokenUtils.saveResult(analysisStoredResult);
                return analysisStoredResult;
            }
            throw new DataFormatException(messages);
        } catch (IOException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

    public AnalysisStoredResult getAnalysisStoredResult(MultipartFile file, HttpServletRequest request){
        if (!file.isEmpty()) {
            try {
                return getAnalysisStoredResult(file.getInputStream(), request);
            } catch (IOException e) {
                throw new UnsupportedMediaTypeException();
            }
        }
        throw new UnsupportedMediaTypeException();
    }

    public AnalysisStoredResult getAnalysisResultFromURL(String url, HttpServletRequest request){
        return getAnalysisStoredResult(getUrlInputStream(url), request);
    }


    private AnalysisStoredResult getAnalysisStoredResult(InputStream input, HttpServletRequest request) {
        try {
            String json = null;
            //When reading from URL, TikaInputStream closes the stream. This is a trick to have fun fun fun
            final byte[] bytes = IOUtils.toByteArray(input);
            String mimeType = detectMimeType(TikaInputStream.get(bytes));
            input = new ByteArrayInputStream(bytes);
            if (isAcceptedContentType(mimeType, "application/zip")) {
                final ZipInputStream zis = new ZipInputStream(input);
                if (zis.getNextEntry() != null) {
                    json = IOUtils.toString(zis, Charset.defaultCharset());
                    final InputStream dataInputStream = new ByteArrayInputStream(json.getBytes());
                    mimeType = detectMimeType(TikaInputStream.get(dataInputStream));
                }
                zis.close();
            } else if (isAcceptedContentType(mimeType, "application/gzip", "application/octet-stream")) {
                final GZIPInputStream inputStream = new GZIPInputStream(input);
                json = IOUtils.toString(inputStream, Charset.defaultCharset());
                final InputStream dataInputStream = new ByteArrayInputStream(json.getBytes());
                mimeType = detectMimeType(TikaInputStream.get(dataInputStream));
            } else {
                json = IOUtils.toString(input, Charset.defaultCharset());
            }
            if (json == null || !isAcceptedContentType(mimeType, "text/plain", "application/json")) {
                throw new UnsupportedMediaTypeException();
            }
            return getAnalysisStoredResult(json, request);
        } catch (IOException e) {
            throw new UnsupportedMediaTypeException();
        }
    }

    private InputStream getUrlInputStream(String url){ //, String... accepts){
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
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {

                throw new UnprocessableEntityException();
            }
            return is;
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

    public Species getSpecies(String species) {
        return speciesService.getSpecies(species.trim().replaceAll("  +", " "));
    }

    public List<Species> getSpeciesList(String species) {
        if (species == null || species.isEmpty()) return null;
        List<Species> rtn = new ArrayList<>();
        for (String s : species.split(",")) {
            Species aux = speciesService.getSpecies(s.trim().replaceAll("  +", " "));
            if (aux != null) rtn.add(aux);
        }
        return rtn;
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

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
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

    private boolean isAcceptedContentType(String contentType, String... accepts){
        return contentType == null || Arrays.stream(accepts).anyMatch(contentType::contains);
    }

    /**
     * #Custom header added to propagate the request protocol when ProxyPass
     * RequestHeader set supports-ssl "true"
     *
     * @param request the request object as provided
     * @return the name of the server with its corresponding protocol
     */
    private String getServerName(HttpServletRequest request){
        String rtn;
        try {
            Boolean supportsSSL = Boolean.valueOf(request.getHeader("supports-ssl"));
            URL url = new URL(request.getRequestURL().toString());
            String protocol = url.getProtocol();
            if(supportsSSL && !protocol.endsWith("s")) protocol += "s";
            rtn = protocol + "://" + url.getHost();
        } catch (MalformedURLException e) {
            rtn = null;
        }
        return rtn;
    }

}
