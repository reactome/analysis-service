package org.reactome.server.analysis.service.utils;

import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.analysis.report.util.GraphCoreConfig;

public class ReactomeGraphConfig {

    public ReactomeGraphConfig(String host, String port, String user, String password) {
        ReactomeGraphCore.initialise(host, port, user,password, GraphCoreConfig.class);
    }
}
