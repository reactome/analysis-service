package org.reactome.server.analysis.service.controller;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class ImporterControllerTest extends AppTests {

    @Before
    public void prepare(){
        generateToken("P02452 P08123 P02461 P12110 P49674 P35222 P09668 Q9NQC7");
    }


    @Test
    public void getPostText() throws Exception {
        String url = String.format("/download/%s/result.json", AppTests.token);
        MvcResult result = mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, null);
        String responseResult = result.getResponse().getContentAsString();

        mockMvcPostResult("/import/", responseResult);

    }

    @Test
    public void getPostFile() throws Exception {

        // generate a content string
        String url = String.format("/download/%s/result.json", AppTests.token);
        MvcResult result = mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE, null);
        String response = result.getResponse().getContentAsString();

        MockMultipartFile importFile = new MockMultipartFile("file", "result.json", "multipart/form-data", response.getBytes());

        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/import/form")
                        .file(importFile);

        this.getMockMvc().perform(builder)
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * the token needs to be included in URL and can not be access in the testing progress, skip this method
     */
   /* @Test
    public void getPostURL(){
        String input = "http://" + AppTests.HOST + ":8080" + "/download/" + AppTests.token + "/result.json";
        mockMvcPostResult("/import/url", input);

    }*/
}