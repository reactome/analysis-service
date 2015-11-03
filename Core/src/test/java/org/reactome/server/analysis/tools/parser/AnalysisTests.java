package org.reactome.server.analysis.tools.parser;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.reactome.server.analysis.parser.InputFormat;
import org.reactome.server.analysis.parser.exception.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Tester class for the Analysis parser
 *
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */

public class AnalysisTests {

    private static String multipleLines = "analysis/multipleLinesSample.txt";
    private static String oneLine = "analysis/singleLineSample.txt";

    /* Increase this value if you want to take an average of multiple runs */
    private int iterations = 1;

    @Test
    public void testOneSingleLineFiles(){
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(oneLine).getFile());
            InputFormat format = parser(file);

            Assert.assertFalse(format.getAnalysisIdentifierSet().isEmpty());

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultipleLinesFiles(){
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(multipleLines).getFile());
            InputFormat format = parser(file);

            Assert.assertFalse(format.getAnalysisIdentifierSet().isEmpty());

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private InputFormat parser(File file) throws IOException {
        InputFormat format = new InputFormat();

        for (int i = 0; i < iterations; i++) {
            InputStream fis = new FileInputStream(file);

            try {
                format.parseData(IOUtils.toString(fis));

            } catch (ParserException p) {
                p.printStackTrace();
            }
        }

        return format;
    }


}

