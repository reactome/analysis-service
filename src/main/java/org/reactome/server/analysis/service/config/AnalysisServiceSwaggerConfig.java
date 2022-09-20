package org.reactome.server.analysis.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.servlet.ServletContext;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Configuration
public class AnalysisServiceSwaggerConfig {

    @Lazy
    @Bean
    public OpenAPI createRestApi(ServletContext context) {
        return new OpenAPI()
                .addServersItem(new Server().url(context.getContextPath()))
                .info(new Info()
                        .title("Pathway Analysis Service")
                        .description("Provides an API for pathway over-representation and expression analysis as well as species comparison tool.")
                        .version("2.0")
                        .license(new License()
                                .name("Creative Commons Attribution 3.0 Unsupported License")
                                .url("https://creativecommons.org/licenses/by/3.0/legalcode"))
                        .termsOfService("/license")
                        .contact(new Contact()
                                .name("Reactome")
                                .email("help@reactome.org")
                                .url("https://reactome.org"))
                );
    }
}