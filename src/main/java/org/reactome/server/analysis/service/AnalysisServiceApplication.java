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

        // BEWARE:
        // If you ran into this exception: InstanceAlreadyExistsException: org.springframework.boot:type=Admin,name=SpringApplication
        // Make sure you disable JMX Agent in Eclipse / IntelliJ
        // https://stackoverflow.com/questions/50436108/javax-management-instancenotfoundexception-org-springframework-boottype-admin

        // fix the IllegalStateException: No Scope registered for scope name 'restart'
        context = new AnnotationConfigApplicationContext(AnalysisServiceApplication.class);
        SpringApplication.run(AnalysisServiceApplication.class, args);
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}
