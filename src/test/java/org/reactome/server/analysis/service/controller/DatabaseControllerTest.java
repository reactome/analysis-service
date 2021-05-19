package org.reactome.server.analysis.service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class DatabaseControllerTest extends AppTests {

    @Test
    public void getBean() {
        findBeanByName("databaseController");
    }

    @Test
    public void getDBName() throws Exception {
        this.getMockMvc().perform(get("/database/name"))
                .andExpect(status().isOk())
                .andExpect(content().string("reactome"))
                .andReturn();
    }

    @Test
    public void getDBVersion() throws Exception {
        // todo null
        mockMvcGetResult("/database/version", "text/plain;charset=ISO-8859-1", null);
    }

    @Test
    public void getDatabaseInfo() throws Exception {
        // todo null
        mockMvcGetResult("/database/info", MediaType.APPLICATION_JSON_VALUE, null);
    }

}