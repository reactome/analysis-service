package org.reactome.server.analysis.report;

import org.apache.log4j.Logger;
import org.reactome.server.analysis.helper.AnalysisHelper.Type;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class AnalysisReport {

    private static Logger logger = Logger.getLogger(AnalysisReport.class.getName());

    private enum AnalysisAction {
        NEW, CACHE
    }

    public static void reportCachedAnalysis(Type type, String name, Boolean toHuman, int ids, int found, long milliseconds){
        report(AnalysisAction.CACHE, type, name, toHuman, ids, found, milliseconds);
    }

//    public static void reportResultDownload(){
//
//    }

    public static void reportNewAnalysis(Type type, String name, Boolean toHuman, int ids, int found, long milliseconds){
        report(AnalysisAction.NEW, type, name, toHuman, ids, found, milliseconds);
    }

    private static void report(AnalysisAction action, Type type, String name, Boolean toHuman, int ids, int found, long milliseconds){
        name = name != null ? " " + name.replaceAll("\\s", "_") : "";
        String toHumanStr = toHuman != null ? ( toHuman ? "toHuman" : "toSpecies") : "N/A";
        StringBuilder message = new StringBuilder(action.toString())
                .append(" ").append(type).append(name)
                .append(" ").append(toHumanStr)
                .append(" size:").append(ids)
                .append(" found:").append(found)
                .append(" time:").append(milliseconds).append("ms");
        logger.info(message);
    }
}
