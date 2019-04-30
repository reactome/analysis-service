package org.reactome.server.analysis.service.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactome.server.analysis.core.model.AnalysisIdentifier;
import org.reactome.server.analysis.core.model.AnalysisReaction;
import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.model.identifier.Identifier;
import org.reactome.server.analysis.core.model.identifier.InteractorIdentifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.model.resource.ResourceFactory;
import org.reactome.server.analysis.core.result.AnalysisSortType;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.core.result.ComparatorFactory;
import org.reactome.server.analysis.core.result.PathwayNodeSummary;
import org.reactome.server.analysis.core.result.external.ExternalAnalysisResult;
import org.reactome.server.analysis.core.result.model.AnalysisSummary;
import org.reactome.server.analysis.core.result.report.AnalysisReport;
import org.reactome.server.analysis.core.result.report.ReportParameters;
import org.reactome.server.analysis.core.util.MapSet;
import org.springframework.core.io.FileSystemResource;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class DownloadHelper {

    private static final String DELIMITER = ",";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static FileSystemResource getHitPathwaysCVS(String filename, AnalysisStoredResult asr, String resource) throws IOException {
        long start = System.currentTimeMillis();
        File f = File.createTempFile(filename, "csv");
        FileWriter fw = new FileWriter(f);

        List<PathwayNodeSummary> pathways = filterPathwaysByResource(asr.getPathways(), resource);
        pathways.sort(getComparator("ENTITIES_PVALUE", "ASC", resource));
        fw.write(getAnalysisResultHeader(asr));
        if (resource.toUpperCase().equals("TOTAL")) {
            for (PathwayNodeSummary summary : pathways) {
                fw.write(getPathwayNodeSummaryTotalRow(asr.getSummary(), summary));
            }
        } else {
            Resource r = ResourceFactory.getResource(resource);
            if (r instanceof MainResource) {
                MainResource mainResource = (MainResource) r;
                for (PathwayNodeSummary summary : pathways) {
                    fw.write(getPathwayNodeSummaryResourceRow(asr.getSummary(), summary, mainResource));
                }
            }
        }
        fw.flush();
        fw.close();

        ReportParameters reportParams = new ReportParameters(asr);
        reportParams.setMilliseconds(System.currentTimeMillis() - start);
        AnalysisReport.reportResultDownload(reportParams);

        return new FileSystemResource(f);
    }

    public static FileSystemResource getIdentifiersFoundMappingCVS(String filename, AnalysisStoredResult asr, String resource) throws IOException {
        long start = System.currentTimeMillis();
        File f = File.createTempFile(filename, "csv");
        FileWriter fw = new FileWriter(f);
        StringBuilder sb = new StringBuilder();

        MapSet<String, MainIdentifier> projection = new MapSet<>();
        if (resource.toUpperCase().equals("TOTAL")) {
            sb.append("Submitted identifier").append(DELIMITER).append("Found identifier").append(DELIMITER).append("Resource\n");
            fw.write(sb.toString());

            for (Identifier identifier : asr.getFoundEntitiesMap().keySet()) {
                projection.add(identifier.getValue().getId(), asr.getFoundEntitiesMap().getElements(identifier));
            }
            for (String identifier : projection.keySet()) {
                for (MainIdentifier mainIdentifier : projection.getElements(identifier)) {
                    //noinspection StringBufferReplaceableByString
                    StringBuilder line = new StringBuilder(identifier);
                    line.append(DELIMITER).append(mainIdentifier.getValue().getId());
                    line.append(DELIMITER).append(mainIdentifier.getResource().getName());
                    line.append("\n");
                    fw.write(line.toString());
                }
            }
        } else {
            sb.append("Submitted identifier").append(DELIMITER).append("Found identifier\n");
            fw.write(sb.toString());
            Resource r = ResourceFactory.getResource(resource);
            if (r instanceof MainResource) {
                MainResource mainResource = (MainResource) r;
                MapSet<Identifier, MainIdentifier> aux = asr.getFoundEntitiesMap(mainResource);
                for (Identifier identifier : aux.keySet()) {
                    projection.add(identifier.getValue().getId(), aux.getElements(identifier));
                }
                for (String identifier : projection.keySet()) {
                    for (MainIdentifier mainIdentifier : projection.getElements(identifier)) {
                        //noinspection StringBufferReplaceableByString
                        StringBuilder line = new StringBuilder(identifier);
                        line.append(DELIMITER).append(mainIdentifier.getValue().getId());
                        line.append("\n");
                        fw.write(line.toString());
                    }
                }
            }
        }
        fw.flush();
        fw.close();

        ReportParameters reportParams = new ReportParameters(asr);
        reportParams.setMilliseconds(System.currentTimeMillis() - start);
        AnalysisReport.reportMappingDownload(reportParams);

        return new FileSystemResource(f);
    }

    public static FileSystemResource getExternalResultsGZIP(String filename, ExternalAnalysisResult er) throws IOException {
        File f = File.createTempFile(filename, "json.gz");
        String json = mapper.writeValueAsString(er);
        OutputStream os = new FileOutputStream(f);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(os);
        gzipOutputStream.write(json.getBytes());
        gzipOutputStream.flush();
        gzipOutputStream.close();
        return new FileSystemResource(f);
    }

    public static FileSystemResource getNotFoundIdentifiers(String filename, AnalysisStoredResult asr) throws IOException {
        long start = System.currentTimeMillis();
        File f = File.createTempFile(filename, "csv");
        FileWriter fw = new FileWriter(f);

        StringBuilder sb = new StringBuilder("Not found");
        for (String col : asr.getExpressionSummary().getColumnNames()) {
            sb.append(DELIMITER).append(col);
        }
        sb.append("\n");
        fw.write(sb.toString());

        for (AnalysisIdentifier analysisIdentifier : asr.getNotFound()) {
            fw.write(analysisIdentifier.getId());
            for (Double val : analysisIdentifier.getExp()) {
                fw.write(DELIMITER);
                fw.write(val.toString());
            }
            fw.write("\n");
        }
        fw.flush();
        fw.close();

        ReportParameters reportParams = new ReportParameters(asr);
        reportParams.setMilliseconds(System.currentTimeMillis() - start);
        AnalysisReport.reportNotFoundDownload(reportParams);

        return new FileSystemResource(f);
    }

    private static String getAnalysisResultHeader(AnalysisStoredResult asr) {
        StringBuilder line = new StringBuilder("Pathway identifier");
        line.append(DELIMITER).append("Pathway name");

        line.append(DELIMITER).append("#Entities found");
        line.append(DELIMITER).append("#Entities total");

        if (asr.getSummary().isInteractors()) {
            line.append(DELIMITER).append("#Interactors found");
            line.append(DELIMITER).append("#Interactors total");
        }

        line.append(DELIMITER).append("Entities ratio");
        line.append(DELIMITER).append("Entities pValue");
        line.append(DELIMITER).append("Entities FDR");
        line.append(DELIMITER).append("#Reactions found");
        line.append(DELIMITER).append("#Reactions total");
        line.append(DELIMITER).append("Reactions ratio");
        for (String colName : asr.getExpressionSummary().getColumnNames()) {
            line.append(DELIMITER).append(colName);
        }

        line.append(DELIMITER).append("Species identifier");
        line.append(DELIMITER).append("Species name");

        line.append(DELIMITER).append("Submitted entities found");
        line.append(DELIMITER).append("Mapped entities");

        if(asr.getSummary().isInteractors()) {
            line.append(DELIMITER).append("Submitted entities hit interactor");
            line.append(DELIMITER).append("Interacts with");
        }

        line.append(DELIMITER).append("Found reaction identifiers");
        return line.append("\n").toString();
    }

    private static String getPathwayNodeSummaryTotalRow(AnalysisSummary analysisSummary, PathwayNodeSummary summary) {
        String stId = summary.getStId();
        String id = (stId != null && !summary.getStId().isEmpty()) ? stId : summary.getPathwayId().toString();
        StringBuilder line = new StringBuilder(id);
        line.append(DELIMITER).append("\"").append(summary.getName()).append("\"");

        PathwayNodeData data = summary.getData();
        line.append(DELIMITER).append(data.getEntitiesFound());
        line.append(DELIMITER).append(data.getEntitiesCount());
        if (analysisSummary.isInteractors()) {
            line.append(DELIMITER).append(data.getInteractorsFound());
            line.append(DELIMITER).append(data.getInteractorsCount());
        }
        line.append(DELIMITER).append(data.getEntitiesRatio());
        line.append(DELIMITER).append(data.getEntitiesPValue());
        line.append(DELIMITER).append(data.getEntitiesFDR());
        line.append(DELIMITER).append(data.getReactionsFound());
        line.append(DELIMITER).append(data.getReactionsCount());
        line.append(DELIMITER).append(data.getReactionsRatio());
        for (Double aDouble : data.getExpressionValuesAvg()) {
            line.append(DELIMITER).append(aDouble);
        }

        line.append(DELIMITER).append(summary.getSpecies().getTaxID());
        line.append(DELIMITER).append(summary.getSpecies().getName());

        Set<String> uniqueSubmitted = new HashSet<>(); //Identifiers differentiates the resources -> using Set<String> to avoid duplicates in the column
        for (Identifier identifier : summary.getData().getIdentifierMap().keySet()) {
            uniqueSubmitted.add(identifier.getValue().getId());
        }
        StringBuilder submitted = new StringBuilder();
        for (String identifier : uniqueSubmitted) {
            submitted.append(identifier).append(";");
        }
        if (submitted.length() > 0) {
            submitted.delete(submitted.length() - 1, submitted.length());
        }
        line.append(DELIMITER).append("\"").append(submitted.toString()).append("\"");

        StringBuilder entities = new StringBuilder();
        if(summary.getData().getInteractorsFound()>0) {
            for (AnalysisIdentifier identifier : summary.getData().getFoundEntities()) {
                entities.append(identifier.getId()).append(";");
            }
        }
        if (entities.length() > 0) {
            entities.delete(entities.length() - 1, entities.length());
        }
        line.append(DELIMITER).append("\"").append(entities.toString()).append("\"");

        if(analysisSummary.isInteractors()) {
            StringBuilder interactors = new StringBuilder();
            StringBuilder interactsWith = new StringBuilder();
            if (data.getInteractorsFound() > 0) {
                for (InteractorIdentifier hitInteractors : data.getInteractorMap().values()) {
                    interactors.append(hitInteractors.getId()).append(";");
                }
                interactors.delete(interactors.length() - 1, interactors.length());
                for (MainIdentifier identifier : data.getInteractorMap().keySet()) {
                    interactsWith.append(identifier.getValue().getId()).append(";");
                }
                interactsWith.delete(interactsWith.length() - 1, interactsWith.length());
            }
            line.append(DELIMITER).append(interactors.toString());
            line.append(DELIMITER).append(interactsWith.toString());
        }

        StringBuilder reactions = new StringBuilder();
        for (AnalysisReaction reaction : summary.getData().getReactions()) {
            reactions.append(reaction.toString()).append(";");
        }
        if (reactions.length() > 0) {
            reactions.delete(reactions.length() - 1, reactions.length());
        }
        line.append(DELIMITER).append("\"").append(reactions.toString()).append("\"");

        return line.append("\n").toString();
    }

    private static String getPathwayNodeSummaryResourceRow(AnalysisSummary analysisSummary, PathwayNodeSummary summary, MainResource resource) {
        String stId = summary.getStId();
        String id = (stId != null && !summary.getStId().isEmpty()) ? stId : summary.getPathwayId().toString();
        StringBuilder line = new StringBuilder(id);
        line.append(DELIMITER).append("\"").append(summary.getName()).append("\"");

        PathwayNodeData data = summary.getData();
        line.append(DELIMITER).append(data.getEntitiesFound(resource));
        line.append(DELIMITER).append(data.getEntitiesCount(resource));

        if (analysisSummary.isInteractors()) {
            line.append(DELIMITER).append(data.getInteractorsFound(resource));
            line.append(DELIMITER).append(data.getInteractorsCount(resource));
        }

        line.append(DELIMITER).append(data.getEntitiesRatio(resource));
        line.append(DELIMITER).append(data.getEntitiesPValue(resource));
        line.append(DELIMITER).append(data.getEntitiesFDR(resource));
        line.append(DELIMITER).append(data.getReactionsFound(resource));
        line.append(DELIMITER).append(data.getReactionsCount(resource));
        line.append(DELIMITER).append(data.getReactionsRatio(resource));
        for (Double aDouble : data.getExpressionValuesAvg(resource)) {
            line.append(DELIMITER).append(aDouble);
        }

        line.append(DELIMITER).append(summary.getSpecies().getTaxID());
        line.append(DELIMITER).append(summary.getSpecies().getName());

        //NOTE: We have to ensure we only add a submitted identifier once per row
        Set<String> uniqueSubmitted = new HashSet<>();
        for (Identifier identifier : summary.getData().getIdentifierMap().keySet()) {
            uniqueSubmitted.add(identifier.getValue().getId());
        }
        StringBuilder submitted = new StringBuilder();
        for (String s : uniqueSubmitted) {
            submitted.append(s).append(";");
        }
        if (submitted.length() > 0) {
            submitted.delete(submitted.length() - 1, submitted.length());
        }
        line.append(DELIMITER).append("\"").append(submitted.toString()).append("\"");

        StringBuilder entities = new StringBuilder();
        for (AnalysisIdentifier identifier : summary.getData().getFoundEntities(resource)) {
            entities.append(identifier.getId()).append(";");
        }
        if (entities.length() > 0) {
            entities.delete(entities.length() - 1, entities.length());
        }
        line.append(DELIMITER).append("\"").append(entities.toString()).append("\"");

        if(analysisSummary.isInteractors()) {
            StringBuilder interactors = new StringBuilder();
            StringBuilder interactsWith = new StringBuilder();
            if (data.getInteractorsFound(resource) > 0) {
                for (InteractorIdentifier hitInteractors : data.getInteractorMap().values()) {
                    interactors.append(hitInteractors.getId()).append(";");
                }
                interactors.delete(interactors.length() - 1, interactors.length());
                for (MainIdentifier identifier : data.getInteractorMap().keySet()) {
                    interactsWith.append(identifier.getValue().getId()).append(";");
                }
                interactsWith.delete(interactsWith.length() - 1, interactsWith.length());
            }
            line.append(DELIMITER).append(interactors.toString());
            line.append(DELIMITER).append(interactsWith.toString());
        }

        StringBuilder reactions = new StringBuilder();
        for (AnalysisReaction reaction : summary.getData().getReactions(resource)) {
            reactions.append(reaction.toString()).append(";");
        }
        if (reactions.length() > 0) {
            reactions.delete(reactions.length() - 1, reactions.length());
        }
        line.append(DELIMITER).append("\"").append(reactions.toString()).append("\"");

        return line.append("\n").toString();
    }

    private static Comparator<PathwayNodeSummary> getComparator(String sortBy, String order, String resource) {
        AnalysisSortType sortType = AnalysisSortType.getSortType(sortBy);
        if (resource != null) {
            Resource r = ResourceFactory.getResource(resource);
            if (r != null && r instanceof MainResource) {
                MainResource mr = (MainResource) r;
                if (order != null && order.toUpperCase().equals("DESC")) {
                    return Collections.reverseOrder(ComparatorFactory.getComparator(sortType, mr));
                } else {
                    return ComparatorFactory.getComparator(sortType, mr);
                }
            }
        }
        if (order != null && order.toUpperCase().equals("DESC")) {
            return Collections.reverseOrder(ComparatorFactory.getComparator(sortType));
        } else {
            return ComparatorFactory.getComparator(sortType);
        }
    }

    private static List<PathwayNodeSummary> filterPathwaysByResource(List<PathwayNodeSummary> pathways, String resource) {
        List<PathwayNodeSummary> rtn;
        if (resource.toUpperCase().equals("TOTAL")) {
            rtn = pathways;
        } else {
            rtn = new LinkedList<>();
            Resource r = ResourceFactory.getResource(resource);
            if (r instanceof MainResource) {
                MainResource mr = (MainResource) r;
                for (PathwayNodeSummary pathway : pathways) {
                    if (pathway.getData().getReactionsFound(mr) > 0) { //reaction aggregates both entities and interactors found
                        rtn.add(pathway);
                    }
                }
            }
        }
        return rtn;
    }
}
