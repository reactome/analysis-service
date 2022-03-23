package org.reactome.server.analysis.service.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Configuration
public class AnalysisServiceSwaggerConfig {

    @Bean
    public OpenAPI createRestApi() {

        return new OpenAPI()
                .info(new Info()
                        .title("Pathway Analysis Service")
                        .description("Provides an API for pathway over-representation and expression analysis as well as species comparison tool.")
                        .version("2.0")
                        .license(new License()
                                .name("Creative Commons Attribution 3.0 Unsupported License")
                                .url("https://creativecommons.org/licenses/by/3.0/legalcode"))
                        .termsOfService("/license")
                        .contact(new Contact()
                                .name("Reaction")
                                .email("help@reactome.org")
                                .url("https://reactome.org"))
                );
    }
}