package org.reactome.server.analysis.service.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactome.server.analysis.core.data.AnalysisData;
import org.reactome.server.analysis.core.result.utils.ExternalAnalysisResultCheck;
import org.reactome.server.analysis.core.result.utils.TokenUtils;
import org.reactome.server.analysis.service.handler.FileDeletorScheduler;
import org.reactome.server.analysis.service.handler.HandlerExceptionResolverImpl;
import org.reactome.server.analysis.service.helper.AnalysisHelper;
import org.reactome.server.analysis.service.helper.FileCheckerController;
import org.reactome.server.analysis.service.utils.ReactomeGraphConfig;
import org.reactome.server.tools.analysis.report.AnalysisReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@PropertySource("classpath:application.properties")
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${analysis.data.file}")
    String fileName;

    @Value("${analysis.data.tmp}")
    String analysisDataTmpPath;


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //todo doesn't help below
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        converters.add(mappingJackson2HttpMessageConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("/resources/");

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(52428800); //10 MB  // 52428800 = 50 MB // 209715200 = 200MB
        return multipartResolver;
    }

    @Bean
    public HandlerExceptionResolverImpl handlerExceptionResolver() {
        return new HandlerExceptionResolverImpl();
    }

    @Bean(destroyMethod = "interrupt")
    public AnalysisData analysisData() {
        AnalysisData analysisData = new AnalysisData();
        analysisData.setFileName(fileName);
        return analysisData;
    }

    @Bean
    public AnalysisHelper analysisHelper() {
        return new AnalysisHelper();
    }

    @Bean
    public ExternalAnalysisResultCheck externalAnalysisResultCheck() {
        return new ExternalAnalysisResultCheck();
    }

    @Bean(destroyMethod = "interrupt", name = "FileCheckerController")
    public FileCheckerController fileCheckerController() {
        FileCheckerController fileCheckerController = new FileCheckerController();
        fileCheckerController.setPathDirectory(analysisDataTmpPath);
        fileCheckerController.setMaxSize(2684354560L); // 2684354560 = 2.5GB // 5368709120 = 5 GB // 10737418240 = 10GB
        fileCheckerController.setThreshold(524288000L); //10485760 = 10MB // 524288000 = 500MB // 1073741824 = 1GB
        fileCheckerController.setTime(10000L); // 10 sec
        fileCheckerController.setTtl(604800000L); // 1 week (SAB suggestion)
        return fileCheckerController;
    }

    @Bean
    public TokenUtils tokenUtils() {
        TokenUtils tokenUtils = new TokenUtils();
        tokenUtils.setPathDirectory(analysisDataTmpPath);
        return tokenUtils;
    }

    @Bean
    public FileDeletorScheduler fileDeletorScheduler() {
        return new FileDeletorScheduler();
    }


    @Bean
    public ReactomeGraphConfig graphCore(@Value("${spring.neo4j.uri}") String uri,
                                         @Value("${spring.neo4j.authentication.username}") String userName,
                                         @Value("${spring.neo4j.authentication.password}") String password) {
        return new ReactomeGraphConfig(uri, userName, password);
    }

    @Bean
    @DependsOn({"graphCore"})
    public AnalysisReport analysisReport(@Value("${diagram.json.folder}") String diagramPath,
                                         @Value("${ehld.folder}") String ehldPath,
                                         @Value("${fireworks.json.folder}") String fireworksPath,
                                         @Value("${analysis.data.tmp}") String analysisDataTmpPath,
                                         @Value("${svg.summary.file}") String svgSummary) {
        return new AnalysisReport(diagramPath, ehldPath, fireworksPath, analysisDataTmpPath, svgSummary);
    }
}
