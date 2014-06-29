package org.reactome.server.analysis.utils;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy;
import org.apache.log4j.Logger;
import org.reactome.server.analysis.helper.AnalysisHelper;
import org.reactome.server.analysis.model.AnalysisSummary;
import org.reactome.server.analysis.report.AnalysisReport;
import org.reactome.server.analysis.result.AnalysisStoredResult;

import java.io.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class ResultDataUtils {

    private static Logger logger = Logger.getLogger(ResultDataUtils.class.getName());

    private static AnalysisStoredResult getAnalysisResult(String fileName) throws FileNotFoundException {
        InputStream file = new FileInputStream(fileName);
        AnalysisStoredResult rtn = (AnalysisStoredResult) ResultDataUtils.read(file);
        logger.info(fileName + " retrieved");
        return rtn;
    }

    public static AnalysisStoredResult getAnalysisResult(AnalysisHelper.Type type, Boolean toHuman, String fileName, boolean report) throws FileNotFoundException {
        long start = -1;
        if(report) {
            start = System.currentTimeMillis();
        }
        AnalysisStoredResult rtn = getAnalysisResult(fileName);
        if(report){
            //The following bit is to create a "nice" report file for future statistics about analysis usage
            AnalysisSummary aux = rtn.getSummary();
            String name = aux.getSampleName();
            if(name==null || name.isEmpty()) name = aux.getFileName();
            if(name==null || name.isEmpty()) name = aux.getSpecies().toString();

            int found = rtn.getFoundEntities().size();
            int notFound = rtn.getNotFound().size();
            int size = found + notFound;
            long end = System.currentTimeMillis();
            AnalysisReport.reportCachedAnalysis(type, name, toHuman, size, found, end - start);
        }
        return rtn;
    }

    public static void kryoSerialisation(AnalysisStoredResult result, String fileName){
        long start = System.currentTimeMillis();
        try {
            Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            OutputStream file = new FileOutputStream(fileName);
            Output output = new Output(file);
            kryo.writeClassAndObject(output, result);

            output.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("%s saved in %d ms", result.getClass().getSimpleName(), end - start));
    }

    private static Object read(InputStream file){
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        Input input = new Input(file);
        Object obj = kryo.readClassAndObject(input);
        input.close();
        return obj;
    }
}
