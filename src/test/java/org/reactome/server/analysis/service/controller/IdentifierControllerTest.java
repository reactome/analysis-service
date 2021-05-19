package org.reactome.server.analysis.service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;


@ExtendWith(SpringExtension.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class IdentifierControllerTest extends AppTests {

    private Map<String, Object> params = new HashMap<>();

    public IdentifierControllerTest() {
        params.put("interactors", false);
        params.put("species", 48887);
        params.put("pageSize", 20);
        params.put("page", 1);
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order", "ASC");
        params.put("pValue", 1);
        params.put("includeDisease", true);
    }

    @Test
    public void getIdentifierToHuman() throws Exception {
        //OVERREPRESENTATION
        mockMvcGetResult("/identifier/P21802", params);
    }

    @Test
    public void getIdentifier() throws Exception {
        //OVERREPRESENTATION
        mockMvcGetResult("/identifier/Q96GD4/projection", params);
    }
}