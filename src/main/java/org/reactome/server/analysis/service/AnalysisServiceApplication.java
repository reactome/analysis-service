package org.reactome.server.analysis.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;


@EntityScan({"org.reactome.server.graph.domain.model"})
@EnableNeo4jRepositories("org.reactome.server.graph.repository")
@SpringBootApplication(scanBasePackages = {"org.reactome.server"})
public class AnalysisServiceApplication extends SpringBootServletInitializer {

    private static ApplicationContext context;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AnalysisServiceApplication.class);
    }

    public static void main(String[] args) {
        // fix the IllegalStateException: No Scope registered for scope name 'restart'
        context = new AnnotationConfigApplicationContext(AnalysisServiceApplication.class);
        SpringApplication.run(AnalysisServiceApplication.class, args);
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}
