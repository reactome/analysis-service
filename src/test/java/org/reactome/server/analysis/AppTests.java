package org.reactome.server.analysis;


import org.reactome.server.analysis.core.model.UserData;
import org.reactome.server.analysis.core.result.AnalysisStoredResult;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(AnalysisHelper.class)
//@ContextConfiguration(classes = TestWebConfig.class)
public abstract class AppTests {

    @Autowired
    private MockMvc mockMvc;

    public static String token;
    public static String stId;


    @Autowired
    public AnalysisHelper analysisHelper;

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    /**
     * Generate a token which is going to be used in DownloadControllerTest,ImporterControllerTest
     * ReportControllerTest,TokenControllerTest
     * <p>
     * Use @Before annotation to execute before each test
     *
     * @param input identifiers
     */
    protected void generateToken(String input) {
        UserData ud = analysisHelper.getUserData(input);
        AnalysisStoredResult asr = analysisHelper.analyse(ud, null, false, true, true);
        AppTests.token = asr.getSummary().getToken();
        AppTests.stId = asr.getPathways().get(0).getStId();
    }

    /**
     * Get request testing of Spring MVC controllers
     */
    protected MvcResult mockMvcGetResult(String url, String contentType, String paramName, String paramValue) throws Exception {
        if (paramName == null && paramValue == null) return mockMvcGetResult(url, contentType, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mockMvcGetResult(url, contentType, params);
    }

    protected MvcResult mockMvcGetResult(String url, Map<String, Object> params) throws Exception {

        if (params != null && !params.isEmpty()) {
            return mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", params);
        } else {
            return mockMvcGetResult(url, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", null);
        }
    }

    protected MvcResult mockMvcGetResult(String url, String contentType, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);

            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());
            //    params.forEach(requestBuilder::param);
            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType))
                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType))
                    .andReturn();
        }
    }


    /**
     * Get request not found testing of Spring MVC controllers
     */
    public MvcResult mockMvcGetResultNotFound(String url, String paramName, String paramValue) throws Exception {

        if (paramName == null && paramValue == null) return mockMvcGetResultNotFound(url, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mockMvcGetResultNotFound(url, params);
    }

    public MvcResult mockMvcGetResultNotFound(String url) throws Exception {
        return mockMvcGetResultNotFound(url, null);
    }

    private MvcResult mockMvcGetResultNotFound(String url, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);
            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());
            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().isNotFound())
                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    get(url))
                    .andExpect(status().isNotFound())
                    .andReturn();
        }
    }


    /**
     * Bad request testing of Spring MVC controllers
     */
    protected MvcResult mockMvcGetResultBadRequest(String url, Map<String, Object> params) throws Exception {
        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = get(url);
            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());
            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().is5xxServerError())
                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    get(url))
                    .andExpect(status().is5xxServerError())
                    .andReturn();
        }
    }


    /**
     * Post request testing of Spring MVC controllers
     */
    protected MvcResult mockMvcPostResult(String url, String content) throws Exception {
        return mockMvcPostResult(url, content, null);
    }

    protected MvcResult mockMvcPostResult(String url, String content, String paramName, String paramValue) throws Exception {
        if (paramName == null && paramValue == null) return mockMvcPostResult(url, content, null);

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return mockMvcPostResult(url, content, params);
    }

    protected MvcResult mockMvcPostResult(String url, String content, Map<String, Object> params) throws Exception {

        if (params != null && !params.isEmpty()) {

            MockHttpServletRequestBuilder requestBuilder = post(url)
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(content);

            for (Map.Entry<String, Object> entry : params.entrySet())
                requestBuilder.param(entry.getKey(), entry.getValue().toString());

            return this.mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"))

                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    post(url)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(content))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"))
                    .andReturn();
        }
    }


    /**
     * post not found request testing of Spring MVC controllers
     */
    protected MvcResult mvcPostResultNotFound(String url, String content) throws Exception {
        return this.mockMvc.perform(
                post(url)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(content))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andReturn();
    }
}
