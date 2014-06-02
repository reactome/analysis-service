package org.reactome.server.analysis.helper;

import org.apache.log4j.Logger;
import org.reactome.server.analysis.exception.ResourceNotFoundException;
import org.reactome.server.analysis.exception.UnsopportedMediaTypeException;
import org.reactome.server.analysis.result.AnalysisStoredResult;
import org.reactome.server.analysis.model.AnalysisSummary;
import org.reactome.server.analysis.utils.ResultDataUtils;
import org.reactome.server.analysis.utils.Tokenizer;
import org.reactome.server.components.analysis.EnrichmentAnalysis;
import org.reactome.server.components.analysis.SpeciesComparison;
import org.reactome.server.components.analysis.model.*;
import org.reactome.server.components.analysis.util.InputUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
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
        EXPRESSION
    }

    private static Logger logger = Logger.getLogger(AnalysisHelper.class.getName());

    private String pathDirectory;

    @Autowired
    private EnrichmentAnalysis enrichmentAnalysis;

    @Autowired
    private SpeciesComparison speciesComparison;

    public AnalysisStoredResult analyse(UserData userData, boolean toHuman){
        return analyse(userData, toHuman, null);
    }

    public AnalysisStoredResult analyse(UserData userData, boolean toHuman, String userFileName){
        Type type =  userData.getExpressionColumnNames().isEmpty() ? Type.OVERREPRESENTATION : Type.EXPRESSION;
        SpeciesNode speciesNode = toHuman ? SpeciesNodeFactory.getHumanNode() : null;
        if(Tokenizer.hasToken(userData.getInputMD5(), toHuman)){
            String token = Tokenizer.getOrCreateToken(userData.getInputMD5(), toHuman);
            AnalysisSummary summary = getAnalysisSummary(token, userData.getSampleName(), type, userFileName);
            try {
                String fileName = getFileName(token);
                return ResultDataUtils.getAnalysisResult(fileName);
            } catch (FileNotFoundException e) {
                logger.trace("No TOKEN found. Analysing...");
                return analyse(summary, userData, speciesNode);
            } catch (Exception e){
                logger.error("Error retrieving the result from the MD5 token. Analysing again...");
            }
        }
        String token = Tokenizer.getOrCreateToken(userData.getInputMD5(), toHuman);
        AnalysisSummary summary = getAnalysisSummary(token, userData.getSampleName(), type, userFileName);
        return analyse(summary, userData, speciesNode);
    }

    public AnalysisStoredResult compareSpecies(Long from, Long to){
        SpeciesNode speciesFrom = SpeciesNodeFactory.getSpeciesNode(from, "");
        SpeciesNode speciesTo = SpeciesNodeFactory.getSpeciesNode(to, "");

        String fakeMD5 = this.getFakedMD5(speciesFrom, speciesTo);
        boolean human = from.equals(SpeciesNodeFactory.getHumanNode().getSpeciesID());
        if(Tokenizer.hasToken(fakeMD5, human)){
            String token = Tokenizer.getOrCreateToken(fakeMD5, human);
            try {
                String fileName = getFileName(token);
                return ResultDataUtils.getAnalysisResult(fileName);
            } catch (FileNotFoundException e) {
                //Nothing here
            }
        }
        UserData ud = speciesComparison.getSyntheticUserData(speciesTo);
        String token = Tokenizer.getOrCreateToken(fakeMD5, human);
        AnalysisSummary summary = new AnalysisSummary(token, null, Type.SPECIES_COMPARISON, to);
        return analyse(summary, ud, speciesFrom);
    }

    private AnalysisSummary getAnalysisSummary(String token, String sampleName, Type type, String userFileName){
        if(userFileName!=null && !userFileName.isEmpty()){
            return new AnalysisSummary(token, sampleName, type, userFileName);
        }else{
            return new AnalysisSummary(token, sampleName, type, true);
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
                //Nothing here
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

    public UserData getUserData(MultipartFile file){
        if(!file.isEmpty()){
            try {
                return InputUtils.getUserData(file.getInputStream());
            } catch (IOException e) {
                throw new UnsopportedMediaTypeException();
            }
        }
        throw new UnsopportedMediaTypeException();
    }

    public void setPathDirectory(String pathDirectory) {
        this.pathDirectory = pathDirectory;
    }

    private AnalysisStoredResult analyse(AnalysisSummary summary, UserData userData, SpeciesNode speciesNode){
        long start = System.currentTimeMillis();
        Set<AnalysisIdentifier> identifiers = userData.getIdentifiers();
        HierarchiesData resAux = this.enrichmentAnalysis.overRepresentation(identifiers, speciesNode);

        AnalysisStoredResult result = new AnalysisStoredResult(userData, resAux);
        result.setSummary(summary);
        result.setHitPathways(resAux.getUniqueHitPathways(speciesNode));
        this.saveResult(result);
        long end = System.currentTimeMillis();
        logger.info(String.format("Analysis for %d identifiers performed in %d ms", identifiers.size(), end-start));
        return result;
    }

    private String getFileName(String token){
        String name = Tokenizer.getName(token);
        return String.format("%s/res_%s.bin", this.pathDirectory, name);
    }

    private void saveResult(final AnalysisStoredResult result){
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String fileName = getFileName(result.getToken());
//                ResultDataUtils.kryoSerialisation(result, fileName);
//            }
//        });
//        thread.start();

        String fileName = getFileName(result.getSummary().getToken());
        ResultDataUtils.kryoSerialisation(result, fileName);
    }
}
