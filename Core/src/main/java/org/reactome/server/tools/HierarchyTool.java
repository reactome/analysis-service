package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
import org.reactome.server.Main;
import org.reactome.server.components.analysis.data.AnalysisData;
import org.reactome.server.components.exporter.HierachyExporter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class HierarchyTool {

    public static void main(String[] args) throws Exception {
        SimpleJSAP jsap = new SimpleJSAP(
                BuilderTool.class.getName(),
                "Provides a set of tools for the pathway analysis and species comparison",
                new Parameter[] {
                        new UnflaggedOption( "tool", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY,
                                "The tool to use. Options: " + Main.Tool.getOptions()) //WE DO NOT TAKE INTO ACCOUNT TOOL HERE ANY MORE
                        ,new FlaggedOption( "type", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 't', "type",
                                "Type of export [DETAILS, RELATIONSHIP]")
                        ,new FlaggedOption( "input", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'i', "input",
                        "The file containing the data structure for the analysis." )
                        ,new QualifiedSwitch( "verbose", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'v', "verbose",
                        "Requests verbose output." )
                }
        );
        JSAPResult config = jsap.parse(args);
        if( jsap.messagePrinted() ) System.exit( 1 );


        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");

        AnalysisData analysisData = context.getBean(AnalysisData.class);
        HierachyExporter exporter = context.getBean(HierachyExporter.class);
        String type = config.getString("type"); type = type != null ? type.toUpperCase() : type;
        switch (type) {
            case "RELATIONSHIP":
                //Initializing Analysis Data  *** IMPORTANT ***
                analysisData.setFileName(config.getString("input"));
                exporter.exportParentship();
                break;
            case "DETAILS":
                //Initializing Analysis Data  *** IMPORTANT ***
                analysisData.setFileName(config.getString("input"));
                exporter.exportDetails();
                break;
            default:
                System.err.println("Wrong export type, please use either DETAILS or RELATIONSHIP");
        }
    }

}
