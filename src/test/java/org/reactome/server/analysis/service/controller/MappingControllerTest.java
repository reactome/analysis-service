package org.reactome.server.analysis.service.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class MappingControllerTest extends AppTests {

    private String overrepresentationData = "#GBM Uniprot\n" +
            "P01023\n" +
            "Q99758\n" +
            "O15439\n" +
            "O43184\n" +
            "Q7Z5R6\n" +
            "P27540\n" +
            "Q13315\n" +
            "P36543\n" +
            "Q13535\n" +
            "Q96GD4\n" +
            "Q13145";

    private String expressionData = "XPO1        \t       1\t        0\t    1\n" +
            "YWHAE       \t       1\t        0\t    1\n" +
            "ZBTB16      \t       1\t        0\t    1\n" +
            "ZCCHC8      \t       1\t        0\t    1\n" +
            "ZNF278      \t       1\t        0\t    1\n" +
            "ZNF331      \t       1\t        0\t    1\n" +
            "ZNF384      \t       1\t        0\t    1\n" +
            "ZNF521      \t       1\t        0\t    1";


    private MockMultipartFile overrepresentationFile = new MockMultipartFile("file", "tuple-mentha-psimitab-ex.txt", "multipart/form-data", overrepresentationData.getBytes());
    private MockMultipartFile expressionFile = new MockMultipartFile("file", "tuple-mentha-psimitab-ex.txt", "multipart/form-data", expressionData.getBytes());

    private String uniProtACFileUrl = "https://raw.githubusercontent.com/Chuqiaoo/reactome-analysis-service-testing-files/master/uniprotACs.txt";
    private String cosmicFileUrl = "https://raw.githubusercontent.com/Chuqiaoo/reactome-analysis-service-testing-files/master/COSMIC.txt";

    @Test
    public void getMappingToHuman() throws Exception {
        //mapping/projection
        mockMvcPostResult("/mapping/projection", overrepresentationData, "interactors", "false");
        mockMvcPostResult("/mapping/projection", expressionData, "interactors", "false");
    }

    @Test
    public void getMapping() throws Exception {
        mockMvcPostResult("/mapping/", overrepresentationData, "interactors", "false");
        mockMvcPostResult("/mapping/", expressionData, "interactors", "false");
    }

    @Test
    public void getMappingPostFileToHuman() throws Exception {

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(overrepresentationFile);
        files.add(expressionFile);

        MockMultipartHttpServletRequestBuilder requestBuilder = fileUpload("/mapping/form/projection");

        for (MockMultipartFile file : files) {
            requestBuilder.file(file);
        }

        this.getMockMvc().perform(requestBuilder
                .param("interactors", "false"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getMappingPostFile() throws Exception {

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(overrepresentationFile);
        files.add(expressionFile);

        MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.fileUpload("/mapping/form");

        for (MockMultipartFile file : files) {
            requestBuilder.file(file);
        }

        this.getMockMvc().perform(requestBuilder.param("interactors", "false")).andExpect(status().isOk()).andReturn();
    }

    @Test
    public void getMappingPostURLToHuman() throws Exception {
        mockMvcPostResult("/mapping/url/projection", uniProtACFileUrl, "interactors", "false");
        mockMvcPostResult("/mapping/url/projection", cosmicFileUrl, "interactors", "false");
    }

    @Test
    public void getMappingPostURL() throws Exception {
        mockMvcPostResult("/mapping/url", uniProtACFileUrl, "interactors", "false");
        mockMvcPostResult("/mapping/url", cosmicFileUrl, "interactors", "false");
    }
}