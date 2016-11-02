package org.reactome.server.analysis.tools;

import com.martiansoftware.jsap.*;
import org.reactome.server.Main;
import org.reactome.server.analysis.core.components.SpeciesComparison;
import org.reactome.server.analysis.core.data.AnalysisData;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ComparisonTool {

    public static void main(String[] args) throws Exception {
        SimpleJSAP jsap = new SimpleJSAP(
                BuilderTool.class.getName(),
                "Provides a set of tools for the pathway analysis and species comparison",
                new Parameter[] {
                        new UnflaggedOption(  "tool",      JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED,  JSAP.NOT_GREEDY,    "The tool to use. Options: " + Main.Tool.getOptions()) //WE DO NOT TAKE INTO ACCOUNT TOOL HERE ANY MORE
                        ,new FlaggedOption(   "structure", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED,    's', "structure", "The file containing the data structure for the analysis." )
                        ,new FlaggedOption(   "output",    JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'o', "output",   "The file where the results are written to." )
                        ,new QualifiedSwitch( "verbose",   JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, 'v', "verbose",  "Requests verbose output." )
                }
        );
        JSAPResult config = jsap.parse(args);
        if( jsap.messagePrinted() ) System.exit( 1 );

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");

        //Initializing Analysis Data  *** IMPORTANT ***
        String structure = config.getString("structure");
        AnalysisData analysisData = context.getBean(AnalysisData.class);
        analysisData.setFileName(structure);

        SpeciesNode speciesFrom = SpeciesNodeFactory.getHumanNode();
        SpeciesNode speciesTo = SpeciesNodeFactory.getSpeciesNode(48892L, "Mus musculus");

        SpeciesComparison comparison = context.getBean(SpeciesComparison.class);
        HierarchiesData hierarchiesData = comparison.speciesComparison(speciesFrom, speciesTo);

        for (PathwayNode node : hierarchiesData.getUniqueHitPathways(speciesFrom)) {
            if(speciesFrom.equals(node.getSpecies())){
                print(node);
            }
        }

        System.out.println(":)");
    }

    private static void print(PathwayNode node){
        String name = node.getName();
        PathwayNodeData data = node.getPathwayNodeData();

        for (MainResource resource : data.getResources()) {
            Integer found = data.getEntitiesFound(resource);
            if(found==0) continue;
            Integer total = data.getEntitiesCount(resource);
            System.out.print(node.getSpecies().getName() + " >> " + resource.getName() + " >> " + name + " (" + found + "/" + total + ")");
            Double pValue = data.getEntitiesPValue(resource); Double ratio = data.getEntitiesRatio(resource); Double fdr = data.getEntitiesFDR(resource);
            if(pValue!=null && ratio!=null && fdr!=null){
                System.out.print("\t" + ratio + "\t" + pValue + "\t" + fdr);
            }

            System.out.print("\t|\t");
            found = data.getReactionsFound(resource);
            total = data.getReactionsCount(resource);
            System.out.println("[" + found + "/" + total + "]");
        }
    }
}
