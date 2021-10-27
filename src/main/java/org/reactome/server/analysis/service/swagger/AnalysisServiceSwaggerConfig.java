package org.reactome.server.analysis.service.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Configuration
@EnableSwagger2
public class AnalysisServiceSwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.reactome.server"))
                .paths(PathSelectors.any())
                .build()
                .tags(new Tag("database", "Database info queries"))
                .tags(new Tag("download", "Methods to download different views of a result"))
                .tags(new Tag("identifier", "Queries for only one identifier"))
                .tags(new Tag("identifiers",  "Queries for multiple identifiers"))
                .tags(new Tag("import", "Imports an external result"))
                .tags(new Tag("mapping", "Identifiers mapping methods"))
                .tags(new Tag("report", "Retrieves report files in PDF format"))
                .tags(new Tag("species",  "Species comparison"))
                .tags(new Tag( "token", "Previous queries filter"))
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Pathway Analysis Service",
                "Provides an API for pathway over-representation and expression analysis as well as species comparison tool",
                "2.0",
                "about/license-agreement",
                new Contact("Reactome","https://reactome.org","help@reactome.org"),
                "Creative Commons Attribution 3.0 Unported License",
                "http://creativecommons.org/licenses/by/3.0/legalcode",
                Collections.emptyList());
    }
}