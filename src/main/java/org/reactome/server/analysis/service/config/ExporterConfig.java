package org.reactome.server.analysis.service.config;

import org.reactome.server.tools.diagram.exporter.common.profiles.service.DiagramExporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;

@Configuration
public class ExporterConfig {
    Logger log = LoggerFactory.getLogger("threadLogger");
    @Autowired
    public ExporterConfig(ServletContext servletContext) {
        String fontPath = servletContext.getRealPath("/resources/fonts");
        if (fontPath != null) {
            log.debug("Configuring Diagram Exporter with font path : " + fontPath);
            DiagramExporterService.configureFontPath(fontPath);
        } else {
            log.error("Font path /resources/fonts could not be resolved from the webapp root");
        }
    }
}