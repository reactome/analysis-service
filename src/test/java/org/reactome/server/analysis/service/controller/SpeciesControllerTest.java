package org.reactome.server.analysis.service.controller;

import org.junit.jupiter.api.Test;
import org.reactome.server.analysis.service.AppTests;

import java.util.HashMap;
import java.util.Map;


public class SpeciesControllerTest extends AppTests {

    @Test
    public void compareHomoSapiensTo() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("pageSize", 20);
        params.put("page", 1);
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order", "ASC");
        params.put("resource", "TOTAL");
        params.put("pValue", 1);
        mockMvcGetResult("/species/homoSapiens/48898", params);
    }
}