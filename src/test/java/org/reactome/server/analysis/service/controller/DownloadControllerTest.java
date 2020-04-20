package org.reactome.server.analysis.service.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class DownloadControllerTest extends AppTests {

    @Before
    public void prepare(){
        generateToken("PTEN");
    }

    @Test
    public void downloadResultCSV() throws Exception {
        String url = String.format("/download/%s/pathways/TOTAL/result.csv", AppTests.token);
        mockMvcGetResult(url, "text/csv",null);
    }

    @Test
    public void downloadResultJSON() throws Exception {
        String url = String.format("/download/%s/result.json", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE,null);
    }

    @Test
    public void downloadResultGZIP() throws Exception {
        String url = String.format("/download/%s/result.json.gz", AppTests.token);
        mockMvcGetResult(url, "application/x-gzip",null);
    }

    @Test
    public void downloadMappingResult() throws Exception {
        String url = String.format("/download/%s/entities/found/TOTAL/result.csv", AppTests.token);
        mockMvcGetResult(url, "text/csv",null);
    }

    @Test
    public void downloadNotFound() throws Exception {
        String url = String.format("/download/%s/entities/notfound/result.csv", AppTests.token);
        mockMvcGetResult(url, "text/csv",null);
    }
}