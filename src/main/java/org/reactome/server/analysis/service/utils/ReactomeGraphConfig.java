package org.reactome.server.analysis.service.utils;

import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.analysis.report.util.AnalysisReportGraphConfig;

public class ReactomeGraphConfig {

    public ReactomeGraphConfig(String uri, String user, String password) {
        ReactomeGraphCore.initialise(uri, user,password, AnalysisReportGraphConfig.class);
    }
}
