package org.reactome.server.analysis.parser;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.reactome.server.analysis.core.model.AnalysisIdentifier;
import org.reactome.server.analysis.parser.exception.ParserException;
import org.reactome.server.analysis.parser.response.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parser for AnalysisData tool
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */
public class InputFormat {

    private static Logger logger = Logger.getLogger(InputFormat.class.getName());

    private List<String> headerColumnNames = new LinkedList<>();
    private Set<AnalysisIdentifier> analysisIdentifierSet = new LinkedHashSet<>();
    private boolean hasHeader = false;
    private final String DELIMITER = "\\s";
    private int thresholdColumn = 0;
    private List<String> errorResponses = new LinkedList<>();
    private List<String> warningResponses = new LinkedList<>();
    private int startOnLine = 0;

    /**
     * This is the core method. Start point for calling other features.
     * It is split in header and data.
     *
     * ParserException is thrown only when there is error.
     *
     * @param input file already converted into a String.
     * @throws IOException, ParserException
     *
     */
    public void parseData(String input) throws IOException, ParserException {
        long start = System.currentTimeMillis();

        if(input == null || input.equalsIgnoreCase("")) {
            // no data to be analysed
            errorResponses.add(Response.getMessage(Response.EMPTY_FILE));
        }else {

            // Split lines
            String[] lines = input.split("\\r?\\n");

            // Prepare header
            analyseHeaderColumns(lines);

            // Prepare content
            analyseContent(lines);

        }

        long end = System.currentTimeMillis();
        logger.debug("Elapsed Time Parsing the data: " + (end - start) + ".ms");

        if(hasError()){
            logger.error("Error analysing your data");
            throw new ParserException("Error analysing your data", errorResponses);
        }

    }

    /**
     * Analyse header based on the first line.
     * Headers must start with # or //
     *
     * @param data
     */
    private void analyseHeaderColumns(String[] data) {
        long start = System.currentTimeMillis();
        String headerLine = "";
        for(int i = 0; i < data.length; i++){
            if(StringUtils.isNotEmpty(data[i])){
                headerLine = data[i];
                startOnLine = i;
                break;
            }
        }

            if (hasHeaderLine(headerLine)) {
                // parse header line
                List<String> headers = getHeaderLabel(headerLine); //TODO: Is it used?
                hasHeader = true;
            } else {
                warningResponses.add(Response.getMessage(Response.MALFORMED_HEADER));
                predictFirstLineAsHeader(headerLine);
            }

        long end = System.currentTimeMillis();
        logger.debug("Elapsed Time on AnalyseHeaderColumns: " + (end - start) + ".ms");
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
            warningResponses.add(Response.getMessage(Response.NO_HEADER));
            buildDefaultHeader(firstLine);
        }
    }

    /**
     * The default header will be built based on the first line.
     *
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
     * Analyse all the data itself.
     * Replace any character like space, comma, semicolon, tab into a space and then replace split by space.
     *
     * @param content line array
     */
    private void analyseContent(String[] content){
        long start = System.currentTimeMillis();
        if(hasHeader){
            startOnLine += 1;
        }

        String regexp = "[\\s,;:\\t]+";

        Pattern p = Pattern.compile(regexp);

        for(int i = startOnLine; i < content.length; ++i){
            String line = content[i];
            if(StringUtils.isBlank(line)){
                warningResponses.add(Response.getMessage(Response.EMPTY_LINE, i + 1));
                continue;
            }

            line = p.matcher(line).replaceAll(" "); // slow slow slow

            String[] data = line.split(DELIMITER);

            if (data.length > 0) {
                // analyse if each line has the same amount of columns as the threshold based on first line
                if (thresholdColumn == data.length) {
                    AnalysisIdentifier rtn = new AnalysisIdentifier(data[0].trim());
                    for (int j = 1; j < data.length; j++) {
                        try {
                            rtn.add(Double.valueOf(data[j].trim()));
                        } catch (NumberFormatException nfe) {
                            warningResponses.add(Response.getMessage(Response.INLINE_PROBLEM, i + 1, j + 1));
                        }
                    }
                    analysisIdentifierSet.add(rtn);
                } else {
                    errorResponses.add(Response.getMessage(Response.COLUMN_MISMATCH, i + 1, thresholdColumn, data.length));
                }
            }
        }

        long end = System.currentTimeMillis();
        logger.debug("Elapsed time on AnalyseContent: " + (end-start) + ".ms");
    }

    private static boolean hasHeaderLine(String line){
        return line.startsWith("#") || line.startsWith("//");
    }

    /**
     * Get header labels and also define a standard pattern in the column length
     *
     * @param line The line to be analysed as a header
     * @return
     */
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

        File analysisDir = new File("/Users/gsviteri/data/Reactome/analysis/sample-files");

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
     *
     * @return true if data is wrong, false otherwise
     */
    public boolean hasError() {
        return errorResponses.size() >= 1;
    }

    /**
     * This is of error messages must be associated with a ParserException.
     *
     * @return list of error messages.
     */
    public List<String> getErrorResponses() {
        return errorResponses;
    }

    /**
     * List of warning messages, process will go on, just show what happened in the parser in terms of
     * blank lines or ignored values.
     *
     * @return list of warning messages.
     */
    public List<String> getWarningResponses() {
        return warningResponses;
    }
}
