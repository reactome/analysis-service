package org.reactome.server.analysis.service.controller;

import org.junit.jupiter.api.Test;
import org.reactome.server.analysis.AppTests;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class IdentifiersControllerTest extends AppTests {

    private String overrepresentationFileContent = "#GBM Uniprot\n" +
            "P01023\n" +
            "Q99758\n" +
            "O15439\n" +
            "O43184\n" +
            "Q13444\n" +
            "P82987\n" +
            "P04083\n" +
            "Q7Z5R6\n" +
            "P27540\n" +
            "Q13315\n" +
            "P36543\n" +
            "Q13535\n" +
            "Q96GD4\n" +
            "Q13145";

    private String expressionFileContent = "#Gene symbol\t#Somatic\t#Germline\tTotal\n" +
            "TP53        \t       8\t        5\t   18\n" +
            "APC         \t       6\t        6\t   17\n" +
            "TERT        \t       9\t        1\t   15\n" +
            "DICER1      \t       8\t        1\t   14\n" +
            "BRAF        \t      12\t        0\t   12\n" +
            "KDM6A       \t       7\t        0\t   12\n" +
            "CUX1        \t       6\t        0\t   11\n" +
            "KRAS        \t       6\t        0\t   11\n" +
            "CTNNB1      \t       5\t        0\t   10\n" +
            "ERBB2       \t       5\t        0\t   10";

    private MockMultipartFile overrepresentationFile = new MockMultipartFile("file", "tuple-mentha-psimitab-ex.txt", "multipart/form-data", overrepresentationFileContent.getBytes());
    private MockMultipartFile expressionFile = new MockMultipartFile("file", "tuple-mentha-psimitab-ex.txt", "multipart/form-data", expressionFileContent.getBytes());
    private Map<String, Object> params = new HashMap<>();

    // this is a constructor
    public IdentifiersControllerTest() {
        params.put("interactors", false);
        //todo return only human species data
        //params.put("species", 48887);
        params.put("pageSize", 20);
        params.put("page", 1);
        params.put("sortBy", "ENTITIES_PVALUE");
        params.put("order", "ASC");
        params.put("pValue", 1);
        params.put("includeDisease", true);
    }

    @Test
    public void getPostTextToHuman() throws Exception {
        String content = "Q32MQ5 Q96L34  O15151 P04198 Q9H1R3 Q6ZWH5 Q99435 Q02548 O43692 P11309 P54278";
        mockMvcPostResult("/identifiers/projection", content, params);
    }

    @Test
    public void getPostText() throws Exception {
        String content = "P02452 P08123 P02461 P12110 P49674 P35222 P09668 Q9NQC7";

        this.getMockMvc().perform(post("/identifiers/").param("interactors", "true").param("includeDisease", "true")
                .contentType(MediaType.TEXT_PLAIN)
                .content(content))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getPostFileToHuman() throws Exception {

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(overrepresentationFile);
        files.add(expressionFile);

        MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/identifiers/form/projection");

        for (MockMultipartFile file : files) {
            requestBuilder.file(file);
        }
        this.getMockMvc().perform(requestBuilder
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("interactors", "false")
                .param("pageSize", "20")
                .param("page", "1")
                .param("sortBy", "ENTITIES_PVALUE")
                .param("order", "ASC")
                .param("resource", "TOTAL")
                .param("pValue", "1")
                .param("includeDisease", "true"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getPostFile() throws Exception {

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(overrepresentationFile);
        files.add(expressionFile);

        MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/identifiers/form/");

        for (MockMultipartFile file : files) {
            requestBuilder.file(file);
        }
        this.getMockMvc().perform(requestBuilder
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("interactors", "false")
                .param("pageSize", "20")
                .param("page", "1")
                .param("sortBy", "ENTITIES_PVALUE")
                .param("order", "ASC")
                .param("resource", "TOTAL")
                .param("pValue", "1")
                .param("includeDisease", "true"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getPostURLToHuman() throws Exception {
        String uniProtACFileUrl = "https://raw.githubusercontent.com/Chuqiaoo/reactome-analysis-service-testing-files/master/uniprotACs.txt";
        mockMvcPostResult("/identifiers/url", uniProtACFileUrl, params);
    }

    @Test
    public void getPostURL() throws Exception {
        String cosmicFileUrl = "https://raw.githubusercontent.com/Chuqiaoo/reactome-analysis-service-testing-files/master/COSMIC.txt";
        mockMvcPostResult("/identifiers/url/projection", cosmicFileUrl, params);
    }
}