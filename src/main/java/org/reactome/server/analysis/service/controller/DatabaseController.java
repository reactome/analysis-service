package org.reactome.server.analysis.service.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.analysis.core.data.AnalysisData;
import org.reactome.server.analysis.core.model.DatabaseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Tag(name = "database", description = "Database info queries")
public class DatabaseController {

    private AnalysisData analysisData;

    @Operation(summary = "The name of current database")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/database/name", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDBName() {
        return analysisData.getDatabaseInfo().getName();
    }


    @Operation(summary = "The version number of current database")
    @ApiResponses({
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/database/version", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDBVersion() {
        return "" + analysisData.getDatabaseInfo().getVersion();
    }


    @Hidden
    @RequestMapping(value = "/database/info", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseInfo getDatabaseInfo() {
        return analysisData.getDatabaseInfo();
    }

    @Autowired
    public void setAnalysisData(AnalysisData analysisData) {
        this.analysisData = analysisData;
    }

}
