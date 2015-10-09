package org.reactome.server.analysis.parser;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.reactome.server.analysis.core.model.AnalysisIdentifier;
import org.reactome.server.analysis.parser.exception.ParserException;
import org.reactome.server.analysis.parser.response.Response;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parser for AnalysisData tool
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */
public class InputFormat {

    private List<String> headerColumnNames = new LinkedList<>();
    private Set<AnalysisIdentifier> analysisIdentifierSet = new LinkedHashSet<>();
    //private String md5;
    private boolean hasHeader = false;
    private final String DELIMITER = "\\s";
    private int thresholdColumn = 0;
    private List<String> errorResponses = new LinkedList<>();
    private List<String> warningResponses = new LinkedList<>();

    /**
     *
     * @param input
     * @throws IOException
     */
    public void parseData(String input) throws IOException, ParserException {
        long start = System.currentTimeMillis();

        if(input == null || input.equalsIgnoreCase("")) {
            // no data to be analysed
            errorResponses.add(Response.getMessage(Response.EMPTY_FILE));
            //parserResponseList.add(ParserResponse.EMPTY_FILE);
        }else {
            // Start parsing
            //md5 = DigestUtils.md5DigestAsHex(input.getBytes());

            // Split lines
            String[] lines = input.split("\\r?\\n");

            // Prepare header
            analyseHeaderColumns(lines);

            // Prepare content
            analyseContent(lines);

        }


        long end = System.currentTimeMillis();
        System.out.println("Parse Time: " + (end - start) + ".ms");

        if(hasError()){
            throw new ParserException("Error analysing your data", errorResponses);
        }

    }

    /**
     * Analyse header based on the first line.
     * Headers must start with # or //
     *
     *
     * @param data
     */
    private void analyseHeaderColumns(String[] data) {
        long start = System.currentTimeMillis();
        String headerLine = "";
        for(int i = 0; i < data.length; i++){
            if(StringUtils.isNotEmpty(data[i])){
                headerLine = data[i];
                break;
            }
        }

        //if(data[0] != null) {
            //String headerLine = firstLine;

            if (hasHeaderLine(headerLine)) {
                // parse header line
                List<String> headers = getHeaderLabel(headerLine);
                hasHeader = true;
            } else {
//                parserResponseList.add(ParserResponse.MALFORMED_HEADER);
                warningResponses.add(Response.getMessage(Response.MALFORMED_HEADER));
                predictFirstLineAsHeader(headerLine);
            }
        //}
        long end = System.currentTimeMillis();
        System.out.println("AnalyseHeaderColumns: " + (end-start) + ".ms");
    }

    /**
     *
     * @param firstLine
     */
    private void predictFirstLineAsHeader(String firstLine) {
        int errorInARow = 0;

        List<String> columnNames = new LinkedList<>();

        String[] data = firstLine.split("\\t");
        if(data.length>0){
            for(int j = 0; j < data.length; j++){
                columnNames.add(data[j].trim());
                try{
                    Double.valueOf(data[j].trim());
                }catch (NumberFormatException nfe){
                    errorInARow++;
                }
            }
        }

        if(errorInARow >= 3){
            hasHeader = true;
            warningResponses.add(Response.getMessage(Response.POTENTIAL_HEADER));

            thresholdColumn = columnNames.size();

            headerColumnNames = columnNames;
        }else {
            // just skip the predictable header and use the default one
//            parserResponseList.add(ParserResponse.NO_HEADER);
            warningResponses.add(Response.getMessage(Response.NO_HEADER));
            buildDefaultHeader(firstLine);
        }
    }

    /**
     * The default header will be built based on the first line.
     * @param firstLine
     */
    private void buildDefaultHeader(String firstLine){
        String line = firstLine.replaceAll("^(#|//)", "");
        List<String> columnNames = new LinkedList<>();
        String[] cols = line.split("\\t");

        thresholdColumn = cols.length;

        columnNames.add("Expression");
        for (int i = 1; i < cols.length; i++) {
            columnNames.add("col_" + i);
        }

        headerColumnNames = columnNames;

    }

    /**
     *
     * @param content
     */
    private void analyseContent(String[] content){
        long start = System.currentTimeMillis();
        int h = 0;
        if(hasHeader){
            h = 1;
        }

        String regexp = "[\\s,;:\\t]+";

        Pattern p = Pattern.compile(regexp);

        for(int i = h; i < content.length; ++i){

            String line = content[i];
            //if(line == null || line.isEmpty()) {
            if(StringUtils.isBlank(line)){
                warningResponses.add(Response.getMessage(Response.EMPTY_LINE, i + 1));
                //parserResponseList.add(ParserResponse.EMPTY_LINE.setParams(i));
                continue;
            }

            line = p.matcher(line).replaceAll(" "); // slow slow slow

            String[] data = line.split(DELIMITER);

            //if(StringUtils.isNotBlank(line)) {
                if (data.length > 0) {
                    // analyse if each line has the same amount of columns as the threshold based on first line
                    if (thresholdColumn == data.length) {
                        AnalysisIdentifier rtn = new AnalysisIdentifier(data[0].trim());
                        for (int j = 1; j < data.length; j++) {
                            try {
                                rtn.add(Double.valueOf(data[j].trim()));
                                analysisIdentifierSet.add(rtn);
                            } catch (NumberFormatException nfe) {
                                warningResponses.add(Response.getMessage(Response.INLINE_PROBLEM, i + 1, j + 1));
                                //parserResponseList.add(ParserResponse.INLINE_PROBLEM.setParams(i, j));
                                //rtn.add(null); //null won't be taken into account for the AVG
                            }
                        }
                    } else {
                        errorResponses.add(Response.getMessage(Response.COLUMN_MISMATCH, i + 1, thresholdColumn, data.length));
                        //parserResponseList.add(ParserResponse.COLUMNS_MISMATCH.setParams(i, thresholdColumn));
                    }
                }
            //}
        }

        long end = System.currentTimeMillis();
        System.out.println("AnalyseContent: " + (end-start) + ".ms");
    }

    /*
    private void printFile(InputStream streamData) throws IOException{
        String input = IOUtils.toString(streamData);

        // Split lines
        String[] lines = input.split("\\r?\\n");
        for(int i = 0; i < lines.length; i++){
            System.out.println("Line: " + i + " - " + lines[i]);

            try {
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    */

    private static boolean hasHeaderLine(String line){
        return line.startsWith("#") || line.startsWith("//");
    }

    private List<String> getHeaderLabel(String line){
        line = line.replaceAll("^(#|//)", "");
        List<String> columnNames = new LinkedList<>();
        String[] cols = line.split("\\t");

        thresholdColumn = cols.length;

        for (String columnName : cols) {
            columnNames.add(StringEscapeUtils.escapeJava(columnName.trim()));
        }

        headerColumnNames = columnNames;

        return columnNames;
    }

    public static void main(String args[]) throws Exception {

        File analysisDir = new File("/Users/gsviteri/data/Reactome/analysis");
        for(File f : analysisDir.listFiles()) {

            System.out.println("Analysing file: " + f.getName() + " - Filesize: " + f.length());

                long start = System.currentTimeMillis();

                InputFormat format = new InputFormat();

                InputStream fis = new FileInputStream(f);

                try {
                    format.parseData(IOUtils.toString(fis));

                    for (String s : format.getWarningResponses()) {
                        System.out.println(s);
                    }

                } catch (ParserException p) {
                    System.out.println(p.getMessage());
                    for (String s : format.getErrorResponses()) {
                        System.out.println(s);
                    }
                }


                long end = System.currentTimeMillis();
                System.out.println("Elapsed Time: " + (end - start) + ".ms\n");

        }
    }

    public List<String> getHeaderColumnNames() {
        return headerColumnNames;
    }

    public Set<AnalysisIdentifier> getAnalysisIdentifierSet() {
        return analysisIdentifierSet;
    }

    /**
     * An easy handy method for determining if the parse succeeded
     * @return true if data is wrong
     */
    public boolean hasError() {
        return errorResponses.size() >= 1;
    }

    public List<String> getErrorResponses() {
        return errorResponses;
    }

    public List<String> getWarningResponses() {
        return warningResponses;
    }
}
