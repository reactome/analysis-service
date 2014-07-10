package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
import org.apache.log4j.Logger;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.server.Main;
import org.reactome.server.components.analysis.filter.AnalysisBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BuilderTool {

    private static Logger logger = Logger.getLogger(BuilderTool.class.getName());


    public static void main(String[] args) throws Exception {
        SimpleJSAP jsap = new SimpleJSAP(
                BuilderTool.class.getName(),
                "Provides a set of tools for the pathway analysis and species comparison",
                new Parameter[] {
                        new UnflaggedOption( "tool", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY,
                                "The tool to use. Options: " + Main.Tool.getOptions()) //WE DO NOT TAKE INTO ACCOUNT TOOL HERE ANY MORE
                        ,new FlaggedOption( "host", JSAP.STRING_PARSER, "localhost", JSAP.NOT_REQUIRED, 'h', "host",
                                "The database host")
                        ,new FlaggedOption( "database", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'd', "database",
                                "The reactome database name to connect to")
                        ,new FlaggedOption( "username", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'u', "username",
                                "The database user")
                        ,new FlaggedOption( "password", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'p', "password",
                                "The password to connect to the database")
                        ,new FlaggedOption( "output", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'o', "output",
                                "The file where the results are written to." )
                        ,new QualifiedSwitch( "verbose", JSAP.STRING_PARSER, "true", JSAP.NOT_REQUIRED, 'v', "verbose",
                                "Requests verbose output." )
                }
        );
        JSAPResult config = jsap.parse(args);
        if( jsap.messagePrinted() ) System.exit( 1 );

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");


        MySQLAdaptor dba = new MySQLAdaptor(
                config.getString("host"),
                config.getString("database"),
                config.getString("username"),
                config.getString("password")
        );
        String fileName = config.getString("output");
        BuilderTool.checkFileName(fileName);

        logger.trace("Starting the data container creation...");
        long start = System.currentTimeMillis();
        AnalysisBuilder builder = context.getBean(AnalysisBuilder.class);
        builder.build(dba, fileName);
        long end = System.currentTimeMillis();
        logger.trace(String.format("Data container creation finished in %d minutes", Math.round((end - start) / 60000L)));
    }

    private static void checkFileName(String fileName){
        File file = new File(fileName);

        if(file.isDirectory()){
            String msg = fileName + " is a folder. Please specify a valid file name.";
            System.err.println(msg);
            logger.fatal(msg);
            System.exit( 1 );
        }

        if(file.getParent()==null){
            file = new File("./" + fileName);
        }
        Path parent = Paths.get(file.getParent());
        if(!Files.exists(parent)){
            String msg = parent + " does not exist.";
            System.err.println(msg);
            logger.fatal(msg);
            System.exit( 1 );
        }

        if(!file.getParentFile().canWrite()){
            String msg = "No write access in " + file.getParentFile();
            System.err.println(msg);
            logger.fatal(msg);
            System.exit( 1 );
        }

        logger.trace(fileName + " is a valid file name");
    }
}
