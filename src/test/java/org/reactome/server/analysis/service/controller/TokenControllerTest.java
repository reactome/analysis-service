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
        Map<String, Object> params = new HashMap<>();
        params.put("species", 48887);
        params.put("pageSize", 20);
        params.put("page", 1);
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order", "ASC");
        params.put("pValue", 1);
        params.put("includeDisease", false);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, params);
    }

    @Test
    public void getTokenFilterPathways() throws Exception {

        // todo post
        // todo input
/*        String url = String.format("/token/%s/filter/pathways", AppTests.token);
        String content = "A comma separated list with the identifiers of the pathways of interest";
        mockMvcPostResult(url, content);*/

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
        Map<String, Object> params = new HashMap<>();
        params.put("pageSize", 20);
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order", "ASC");
        params.put("resource", "TOTAL");
        params.put("pValue", 1);
        params.put("includeDisease", true);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, params);

    }

    @Test
    public void getTokenHitEntitiesPathway() throws Exception {
        String url = String.format("/token/%s/found/all/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "resource", "TOTAL");
    }

    @Test
    public void getTokenHitEntitiesPathways() {
        //todo post
        //todo input
        //String url = String.format("/token/%s/found/all", AppTests.token);
    }

    @Test
    public void getTokenIdentifiersPathway() throws Exception {
        String url = String.format("/token/%s/found/entities/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "page", "1");
    }

    /* API ignored*/

    @Test
    public void getTokenSummaryPathway() throws Exception {
        String url = String.format("/token/%s/summary/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "page", "1");
    }

    @Test
    public void getTokenInteractorsPathway() throws Exception {
        //todo check content type
        //String url = String.format("/token/%s/found/interactors/%s", AppTests.token, AppTests.stId);
        String url = String.format("/token/%s/found/interactors/%s", "MjAyMDAzMTIwOTE4MjBfMw%3D%3D", "R-HSA-8948216");
        Map<String, Object> params = new HashMap<>();
        params.put("pageSize", 20);
        params.put("page", 1);
        params.put("resource", "TOTAL");
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, params);
    }

    @Test
    public void getNotFoundIdentifiers() throws Exception {
        String url = String.format("/token/%s/notFound", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
    }

    @Test
    public void getTokenFilterPathwayReactions() throws Exception {
        String url = String.format("/token/%s/reactions/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "resource", "TOTAL");
    }

    @Test
    public void getTokenFilterPathwaysReactions() {
        //post
        //todo input
        //String url = String.format("/token/%s/reactions/pathways", AppTests.token);

    }

    @Test
    public void getResources() throws Exception {
        String url = String.format("/token/%s/resources", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
    }

    @Test
    public void getPathwaysBinnedBySize() throws Exception {
        String url = String.format("/token/%s/pathways/binned", AppTests.token);
        Map<String, Object> params = new HashMap<>();
        params.put("binSize", 100);
        params.put("resource", "TOTAL");
        params.put("pValue", 1);
        params.put("species", 48887);
        params.put("includeDisease", true);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_UTF8_VALUE, "resource", "TOTAL");
    }
}