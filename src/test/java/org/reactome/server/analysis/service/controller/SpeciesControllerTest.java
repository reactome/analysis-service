package org.reactome.server.analysis.service.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class SpeciesControllerTest extends AppTests {

    @Test
    public void compareHomoSapiensTo() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("pageSize",20);
        params.put("page",1);
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order","ASC");
        params.put("resource","TOTAL");
        params.put("pValue",1);
        mockMvcGetResult("/species/homoSapiens/48898",params);
    }
}