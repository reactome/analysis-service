package org.reactome.server.analysis.service.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;


public class TokenControllerTest extends AppTests {

    @BeforeEach
    public void prepare() {
        generateToken("P02452 P08123 P02461 P12110 P49674 P35222 P09668 Q9NQC7");
    }

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
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, params);
    }

    @Test
    public void getTokenFilterPathways() throws Exception {

        String url = String.format("/token/%s/filter/pathways", AppTests.token);
        Map<String, Object> params = new HashMap<>();
        params.put("resource", "TOTAL");
        params.put("pValue", 1);
        params.put("includeDisease", false);
        String input = AppTests.stId;
        mockMvcPostResult(url, input, params);
    }

    @Test
    public void filterBySpecies() throws Exception {
        String url = String.format("/token/%s/filter/species/48887", AppTests.token);
        Map<String, Object> params = new HashMap<>();
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order", "ASC");
        params.put("resource", "TOTAL");
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, params);
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
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, params);
    }

    @Test
    public void getTokenHitEntitiesPathway() throws Exception {
        String url = String.format("/token/%s/found/all/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, "resource", "TOTAL");
    }

    @Test
    public void getTokenHitEntitiesPathways() throws Exception {
        String url = String.format("/token/%s/found/all", AppTests.token);
        String input = AppTests.stId;
        mockMvcPostResult(url, input);
    }

    @Test
    public void getTokenIdentifiersPathway() throws Exception {
        String url = String.format("/token/%s/found/entities/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, "page", "1");
    }

    /* API ignored*/
    @Test
    public void getTokenSummaryPathway() throws Exception {
        String url = String.format("/token/%s/summary/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, "page", "1");
    }

    @Test
    public void getTokenInteractorsPathway() throws Exception {
        String url = String.format("/token/%s/found/interactors/%s", AppTests.token, "3000178");
        Map<String, Object> params = new HashMap<>();
        params.put("pageSize", 20);
        params.put("page", 1);
        params.put("resource", "TOTAL");
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, params);
    }

    @Test
    public void getNotFoundIdentifiers() throws Exception {
        String url = String.format("/token/%s/notFound", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, null);
    }

    @Test
    public void getTokenFilterPathwayReactions() throws Exception {
        String url = String.format("/token/%s/reactions/%s", AppTests.token, AppTests.stId);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, "resource", "TOTAL");
    }

    @Test
    public void getTokenFilterPathwaysReactions() throws Exception {

        String url = String.format("/token/%s/reactions/pathways", AppTests.token);
        String input = AppTests.stId + "," + "R-HSA-8948216";
        Map<String, Object> params = new HashMap<>();
        params.put("resource", "TOTAL");
        params.put("pValue", 1);
        params.put("species", 48887);
        params.put("includeDisease", false);
        mockMvcPostResult(url, input, params);
    }

    @Test
    public void getResources() throws Exception {
        String url = String.format("/token/%s/resources", AppTests.token);
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, null);
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
        mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, params);
    }
}