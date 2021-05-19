package org.reactome.server.analysis.service.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class ReportControllerTest extends AppTests {

    @BeforeEach
    public void prepare() {
        generateToken("PTEN");
    }

    @Test
    public void generatePdfReport() throws Exception {
        String url = String.format("/report/%s/Homo sapiens/result.pdf", AppTests.token);
        Map<String, Object> params = new HashMap<>();
        params.put("number", 25);
        params.put("resource", "TOTAL");
        params.put("diagramProfile", "Modern");
        params.put("analysisProfile", "Standard");
        params.put("fireworksProfile", "Cooper");
        mockMvcGetResult(url, MediaType.APPLICATION_PDF_VALUE, params);
    }
}