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
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parser for AnalysisData tool
 *
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */
public class InputFormat {

    private static Logger logger = Logger.getLogger(InputFormat.class.getName());

    private List<String> headerColumnNames = new LinkedList<>();
    private Set<AnalysisIdentifier> analysisIdentifierSet = new LinkedHashSet<>();
    private boolean hasHeader = false;

    /**
     * Threshold number for columns, based on the first line we count columns.
     * All the following lines must match this threshold.
     */
    private int thresholdColumn = 0;

    private List<String> errorResponses = new LinkedList<>();
    private List<String> warningResponses = new LinkedList<>();

    /**
     * Ignoring the initial blank lines and start parsing from the
     * first valid line.
     */
    private int startOnLine = 0;

    /**
     * This is the core method. Start point for calling other features.
     * It is split in header and data.
     *
     * ParserException is thrown only when there are errors.
     *
     * @param input file already converted into a String.
     * @throws IOException, ParserException
     */
    public void parseData(String input) throws IOException, ParserException {
        long start = System.currentTimeMillis();

        if (input == null || input.equalsIgnoreCase("")) {
            // no data to be analysed
            errorResponses.add(Response.getMessage(Response.EMPTY_FILE));
        } else {
            // Split lines
            String[] lines = input.split("\\r?\\n");

            // check and parser whether one line file is present.
            boolean isOneLine = analyseOneLineFile(lines);
            if (!isOneLine) {
                // Prepare header
                analyseHeaderColumns(lines);

                // Prepare content
                analyseContent(lines);
            }

        }

        long end = System.currentTimeMillis();
        logger.debug("Elapsed Time Parsing the data: " + (end - start) + ".ms");

        if (hasError()) {
            logger.error("Error analysing your data");
            throw new ParserException("Error analysing your data", errorResponses);
        }

    }

    /**
     * ---- FOR SOME SPECIFIC CASES ----
     * There're cases where the user inputs a file with one single line to be analysed
     * This method performs a quick view into the file and count the lines. It stops if file has more than one line.
     * p.s empty lines are always ignored
     * To avoid many iteration to the same file, during counting lines the main attributes are being set and used in the
     * analyse content method.
     * This method ignores blank lines,spaces, tabs and so on.
     *
     * @param input the file
     * @return true if file has one line, false otherwise.
     */
    private boolean analyseOneLineFile(String[] input) {
        int countNonEmptyLines = 0;
        String validLine = "";
        for (int i = 0; i < input.length; i++) {
            // Cleaning the line in other to eliminate blank or spaces spread in the file
            String cleanLine = input[i].replaceAll("[\\s,;:]+", "");
            if (StringUtils.isNotEmpty(cleanLine) || StringUtils.isNotBlank(cleanLine)) {
                countNonEmptyLines++;

                // Line without parsing - otherwise we can't eliminate blank space and tabs spread in the file.
                validLine = input[i];

                // We don't need to keep counting...
                if (countNonEmptyLines > 1) {
                    return false;
                }
            }
        }

        hasHeader = false;
        thresholdColumn = validLine.split("[\\s,;:]+").length;

        analyseContent(new String[]{validLine});

        return true;
    }

    /**
     * Analyse header based on the first line.
     * Headers must start with # or //
     *
     * @param data is the file lines
     */
    private void analyseHeaderColumns(String[] data) {
        long start = System.currentTimeMillis();

        /**
         * Verify in which line the file content starts. Some cases, file has a bunch of blank line in the firsts lines.
         * StartOnLine will be important in the content analysis. Having this attribute we don't to iterate and ignore
         * blank lines in the beginning.
         */
        String headerLine = "";
        for (int i = 0; i < data.length; i++) {
            if (StringUtils.isNotEmpty(data[i])) {
                headerLine = data[i];
                startOnLine = i;
                break;
            }
        }

        if (hasHeaderLine(headerLine)) {
            // parse header line
            getHeaderLabel(headerLine);
            hasHeader = true;
        } else {
            //warningResponses.add(Response.getMessage(Response.MALFORMED_HEADER));
            predictFirstLineAsHeader(headerLine);
        }

        long end = System.currentTimeMillis();
        logger.debug("Elapsed Time on AnalyseHeaderColumns: " + (end - start) + ".ms");
    }

    /**
     * There're files which may have a header line but malformed.
     * This method analyse the first line and if the columns are not number
     * a potential header is present and the user will be notified
     *
     * @param firstLine potential header
     */
    private void predictFirstLineAsHeader(String firstLine) {
        int errorInARow = 0;

        List<String> columnNames = new LinkedList<>();

        firstLine = firstLine.replaceAll("^(#|//)", "");
        String[] data = firstLine.split("[\\s,;:]+");

        if (data.length > 0) {
            for (String col : data) {
                columnNames.add(col.trim());
                try {
                    Double.valueOf(col.trim());
                } catch (NumberFormatException nfe) {
                    errorInARow++;
                }
            }
        }

        if (errorInARow >= 3) {
            hasHeader = true;
            warningResponses.add(Response.getMessage(Response.POTENTIAL_HEADER));

            thresholdColumn = columnNames.size();

            headerColumnNames = columnNames;
        } else {
            // just skip the predictable header and use the default one
            warningResponses.add(Response.getMessage(Response.NO_HEADER));
            //buildDefaultHeader(firstLine);
            buildDefaultHeader(data);
        }
    }

    /**
     * The default header will be built based on the first line.
     *
     * @param cols
     */
    private void buildDefaultHeader(String[] cols) {
        thresholdColumn = cols.length;

        headerColumnNames.add("Probeset");
        for (int i = 1; i < cols.length; i++) {
            headerColumnNames.add("col" + i);
        }
    }

    /**
     * Analyse all the data itself.
     * Replace any character like space, comma, semicolon, tab into a space and then replace split by space.
     *
     * @param content line array
     */
    private void analyseContent(String[] content) {
        long start = System.nanoTime();
        if (hasHeader) {
            startOnLine += 1;
        }

        /** Note also that using + instead of * avoids replacing empty strings and therefore might also speed up the process. **/
        String regexp = "[\\s,;:]+";

        Pattern p = Pattern.compile(regexp);

        for (int i = startOnLine; i < content.length; ++i) {
            String line = content[i];
            if (StringUtils.isBlank(line)) {
                warningResponses.add(Response.getMessage(Response.EMPTY_LINE, i + 1));
                continue;
            }

            /** Note that using String.replaceAll() will compile the regular expression each time you call it. **/
            line = p.matcher(line).replaceAll(" "); // slow slow slow

            // StringTokenizer has more performance to offer than String.slit.
            StringTokenizer st = new StringTokenizer(line); //space is default delimiter.

            int tokens = st.countTokens();
            if (tokens > 0) {
                /**
                 * analyse if each line has the same amount of columns as the threshold based on first line, otherwise
                 * an error will be reported.
                 */
                if (thresholdColumn == tokens) {
                    String first = st.nextToken();
                    AnalysisIdentifier rtn = new AnalysisIdentifier(first);
                    int j = 1;
                    while (st.hasMoreTokens()) {
                        try {
                            rtn.add(Double.valueOf(st.nextToken().trim()));
                        } catch (NumberFormatException nfe) {
                            warningResponses.add(Response.getMessage(Response.INLINE_PROBLEM, i + 1, j + 1));
                        }
                        j++;
                    }
                    analysisIdentifierSet.add(rtn);
                } else {
                    errorResponses.add(Response.getMessage(Response.COLUMN_MISMATCH, i + 1, thresholdColumn, tokens));
                }
            }
        }

        long end = System.nanoTime();
        logger.debug("Elapsed time on AnalyseContent: " + (end - start) + ".ms");
    }

    private static boolean hasHeaderLine(String line) {
        return line.startsWith("#") || line.startsWith("//");
    }

    /**
     * Get header labels and also define a standard pattern in the column length
     *
     * @param line The line to be analysed as a header
     */
    private void getHeaderLabel(String line) {
        // remove chars which categorizes a comment.
        line = line.replaceAll("^(#|//)", "");

        // Split header line by our known delimiters
        String[] cols = line.split("[\\s,;:]+");

        thresholdColumn = cols.length;

        for (String columnName : cols) {
            headerColumnNames.add(StringEscapeUtils.escapeJava(columnName.trim()));
        }
    }

    public static void main(String args[]) throws Exception {

        File analysisDir = new File("/Users/gsviteri/Google Drive/Reactome/analysis/sample-files/");

//        File f = new File("/Users/gsviteri/Google Drive/Reactome/analysis/sample-files/onesingleline.txt");

        Map<String, Long> results = new TreeMap<>();

        for (int i = 0; i < 5; i++) {
            for (File f : analysisDir.listFiles()) {
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
                    for (String s : p.getErrorMessages()) {
                        System.out.println(s);
                    }
                }

                long end = System.currentTimeMillis();

                System.out.println("Elapsed Time: " + (end - start) + ".ms\n");

                if (results.containsKey(f.getName())) {
                    Long x = results.get(f.getName());
                    Long t = (end - start) + x;
                    results.put(f.getName(), t);
                } else {
                    results.put(f.getName(), (end - start));
                }
            }
        }

        for (String key : results.keySet()) {
            Long x = results.get(key) / 5L;
            System.out.println(key + " avg. " + x);

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
