package org.reactome.server.analysis.service.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class ReportControllerTest extends AppTests{

    @Before
    public void prepare(){
        generateToken("PTEN");
    }

    @Test
    public void generatePdfReport() throws Exception {
        String url = String.format("/report/%s/Homo sapiens/result.pdf", AppTests.token);
        Map<String, Object> params = new HashMap<>();
        params.put("number",25);
        params.put("resource","TOTAL");
        params.put("diagramProfile","Modern");
        params.put("analysisProfile","Standard");
        params.put("fireworksProfile","Cooper");
        mockMvcGetResult(url, MediaType.APPLICATION_PDF_VALUE, params);
    }
}