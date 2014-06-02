package org.reactome.server.analysis.utils;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy;
import org.apache.log4j.Logger;
import org.reactome.server.analysis.result.AnalysisStoredResult;

import java.io.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class ResultDataUtils {

    private static Logger logger = Logger.getLogger(ResultDataUtils.class.getName());

    public static AnalysisStoredResult getAnalysisResult(InputStream file){
//        logger.info(String.format("Loading %s file...", AnalysisResult.class.getSimpleName()));
        long start = System.currentTimeMillis();
        AnalysisStoredResult result = (AnalysisStoredResult) ResultDataUtils.read(file);
        long end = System.currentTimeMillis();
        logger.info(String.format("%s file loaded in %d ms", AnalysisStoredResult.class.getSimpleName() , end-start));
        return result;
    }

    public static AnalysisStoredResult getAnalysisResult(String fileName) throws FileNotFoundException {
        InputStream file = new FileInputStream(fileName);
        return getAnalysisResult(file);
    }

    public static void kryoSerialisation(AnalysisStoredResult result, String fileName){
//        logger.trace(String.format("Saving %s data into file %s", result.getClass().getSimpleName(), fileName));
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
