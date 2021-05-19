package org.reactome.server.analysis.service.controller;

import io.swagger.annotations.*;
import org.reactome.server.analysis.core.data.AnalysisData;
import org.reactome.server.analysis.core.model.DatabaseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@Api(tags={"database"})
public class DatabaseController {

    private AnalysisData analysisData;

    @ApiOperation(value = "The name of current database")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/database/name", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDBName()  {
        return analysisData.getDatabaseInfo().getName();
    }


    @ApiOperation(value = "The version number of current database")
    @ApiResponses({
            @ApiResponse(code = 406, message = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/database/version", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getDBVersion()  {
        return "" + analysisData.getDatabaseInfo().getVersion();
    }


    @ApiIgnore
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
