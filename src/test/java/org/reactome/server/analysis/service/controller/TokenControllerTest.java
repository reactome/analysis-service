package org.reactome.server.analysis.service.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class TokenControllerTest extends AppTests {

    @Test
    public void getToken() throws Exception {
        String url = String.format("/token/%s/", AppTests.token);
        //todo parameters are not required
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "pageSize", "20");
    }

    @Test
    public void getTokenFilterPathways() {

        // token/{token}/filter/pathways
        // todo post
        // input parameter is required
        // String url = String.format("/token/%s/filter/pathways", AppTests.token);

    }

    @Test
    public void filterBySpecies() throws Exception {
        String url = String.format("/token/%s/filter/species/48887", AppTests.token);
        Map<String, Object> params = new HashMap<>();
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order", "ASC");
        params.put("resource", "TOTAL");
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, params);
    }

    @Test
    public void getPageOfPathway() throws Exception {
        String url = String.format("/token/%s/page/%s", AppTests.token, AppTests.stId);
        //todo parameters are not required
        //todo pathway id is not a static ID
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, null);

    }

    @Test
    public void getTokenHitEntitiesPathway() throws Exception {
        ///{token}/found/all/{pathway}
        //todo pathway id is not a static ID
        String url = String.format("/token/%s/found/all/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "resource", "TOTAL");
    }

    @Test
    public void getTokenHitEntitiesPathways() {
        //todo post
        //todo input
        //  String url = String.format("/token/%s/found/all", AppTests.token);
    }

    @Test
    public void getTokenIdentifiersPathway() throws Exception {
        ///{token}/found/entities/{pathway}
        //todo pathway id is not a static ID
        String url = String.format("/token/%s/found/entities/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "page", "1");
    }

    @Test
    public void getTokenSummaryPathway() {
        ///{token}/summary/{pathway}
        //todo API ignore
        // String url = String.format("/token/%s/summary/R-HSA-5669034", AppTests.token);
    }

    @Test
    public void getTokenInteractorsPathway() {
        //get
        // String url = String.format("/token/%s/found/interactors/R-HSA-5669034", AppTests.token);
        //todo input and response content
        //todo use the url above, the response body is empty
    }

    @Test
    public void getNotFoundIdentifiers() throws Exception {
        ///{token}/notFound
        //todo check the response
        String url = String.format("/token/%s/notFound", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
    }

    @Test
    public void getTokenFilterPathwayReactions() throws Exception {
        ///{token}/reactions/{pathway}
        //todo pathway in path; check the result
        String url = String.format("/token/%s/reactions/%s", AppTests.token, AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "resource", "TOTAL");
    }

    @Test
    public void getTokenFilterPathwaysReactions() {
        ///{token}/reactions/pathways
        //post
        //todo input
    }

    @Test
    public void getResources() throws Exception {
        // todo check the response result
        String url = String.format("/token/%s/resources", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
    }

    @Test
    public void getPathwaysBinnedBySize() throws Exception {
        //todo parameters are not required
        String url = String.format("/token/%s/pathways/binned", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "resource", "TOTAL");
    }
}