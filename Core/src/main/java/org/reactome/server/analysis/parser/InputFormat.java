package org.reactome.server.analysis.parser;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.reactome.server.analysis.core.model.AnalysisIdentifier;
import org.reactome.server.analysis.parser.exception.ParserException;
import org.reactome.server.analysis.parser.response.Response;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parser for AnalysisData tool
 *
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */
public class InputFormat {

    private static Logger logger = Logger.getLogger(InputFormat.class.getName());

    /**
     * This is the default header for oneline file and multiple line file
     * Changing here will propagate in both.
     */
    private static final String DEFAULT_IDENTIFIER_HEADER = "";
    private static final String DEFAULT_EXPRESSION_HEADER = "col";

    /** Pride is using colon, we decided to remove it from the parser **/
    private static final String HEADER_SPLIT_REGEX = "[\\t,;]+";

    /** Regex for parsing the content when we do not have the header, trying to build a default one **/
    private static final String NO_HEADER_DEFAULT_REGEX = "[\\s,;]+";

    /** Regex used to split the content of a single line file **/
    private static final String ONE_LINE_CONTENT_SPLIT_REGEX = "[\\s,;]+";

    /** Regex used to split the content of a multiple line file **/
    private static final String MULTI_LINE_CONTENT_SPLIT_REGEX = "[\\s,;]+";

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
     * <p>
     * ParserException is thrown only when there are errors.
     *
     * @param input file already converted into a String.
     * @throws IOException, ParserException
     */
    public void parseData(String input) throws IOException, ParserException {
        long start = System.currentTimeMillis();

        String clean = input.trim();
        if (clean.equalsIgnoreCase("")) {
            // no data to be analysed
            errorResponses.add(Response.getMessage(Response.EMPTY_FILE));
        } else {
            // Split lines
            String[] lines = input.split("[\r\n]"); // Do not add + here. It will remove empty lines

            // check and parser whether one line file is present.
            boolean isOneLine = isOneLineFile(lines);
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
     * ---- FOR VERY SPECIFIC CASES, BUT VERY USEFUL FOR REACTOME ----
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
    private boolean isOneLineFile(String[] input) {

        int countNonEmptyLines = 0;
        String validLine = "";

        for (int i = 0; i < input.length; i++) {
            // Cleaning the line in other to eliminate blank or spaces spread in the file
            String cleanLine = input[i].trim();
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

        analyseOneLineFile(validLine);

        return true;
    }

    /**
     * For single line files the rules are:
     * - No Expressions Values
     * - Cannot start with #, comments
     * - Must match this "regular expression" (\delim)?ID((\delimID)* | (\delimNUMBER)* )
     *     - where \delim can be space, comma, colon, semi-colon or tab.
     */
    private void analyseOneLineFile(String line) {
        long start = System.nanoTime();

        /** Note also that using + instead of * avoids replacing empty strings and therefore might also speed up the process. **/
        String regexp = ONE_LINE_CONTENT_SPLIT_REGEX;

        Pattern p = Pattern.compile(regexp);

        // Line cannot start with # or //
        if (hasHeaderLine(line)) {
            errorResponses.add(Response.getMessage(Response.START_WITH_HASH));
            return;
        }

        /** Note that using String.replaceAll() will compile the regular expression each time you call it. **/
        line = p.matcher(line).replaceAll(" ");

        /** StringTokenizer has more performance to offer than String.slit. **/
        StringTokenizer st = new StringTokenizer(line); //space is default delimiter.

        int tokens = st.countTokens();
        if (tokens > 0) {
            headerColumnNames.add(DEFAULT_IDENTIFIER_HEADER);
            while (st.hasMoreTokens()) {
                AnalysisIdentifier rtn = new AnalysisIdentifier(st.nextToken().trim());
                analysisIdentifierSet.add(rtn);
            }
        }

        long end = System.nanoTime();
        logger.debug("Elapsed time on AnalyseContent: " + (end - start) + ".ms");
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

        /**
         * We cannot use the HEADER REGEX for parsing the header and prepare the default
         * Why? Tab is a delimiter for header, but space isn't. Colon is a delimiter fo the content
         * but not for the header.
         **/
        String[] data = firstLine.split(NO_HEADER_DEFAULT_REGEX);

        if (data.length > 0) {
            for (String col : data) {
                columnNames.add(col.trim());
                if(!isNumeric(col.trim())){
                    errorInARow++;
                }
            }
        }

        thresholdColumn = data.length;

        if (errorInARow >= 3) {
            hasHeader = true;
            warningResponses.add(Response.getMessage(Response.POTENTIAL_HEADER));

            headerColumnNames = columnNames;
        } else {
            // just skip the predictable header and use the default one
            warningResponses.add(Response.getMessage(Response.NO_HEADER));

            buildDefaultHeader(data.length);
        }
    }

    /**
     * The default header will be built based on the first line.
     *
     * @param colsLength
     */
    private void buildDefaultHeader(Integer colsLength) {
        thresholdColumn = colsLength;

        headerColumnNames.add(DEFAULT_IDENTIFIER_HEADER);
        for (int i = 1; i < colsLength; i++) {
            headerColumnNames.add(DEFAULT_EXPRESSION_HEADER + i);
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
        String regexp = MULTI_LINE_CONTENT_SPLIT_REGEX;

        Pattern p = Pattern.compile(regexp);

        for (int i = startOnLine; i < content.length; ++i) {
            String line = content[i].trim();
            if (line.isEmpty()) {
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
                        String token = st.nextToken().trim();
                        if(isNumeric(token)){
                            rtn.add(Double.valueOf(token));
                        }else {
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
        String[] cols = line.split(HEADER_SPLIT_REGEX);

        thresholdColumn = cols.length;

        for (String columnName : cols) {
            headerColumnNames.add(StringEscapeUtils.escapeJava(columnName.trim()));
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

    /**
     * Instead of calling the Double.valueOf(...) in a try-catch statement and many of the checks to fail due to
     * not being a number then performance of this mechanism will not be great, since you're relying upon
     * exceptions being thrown for each failure, which is a fairly expensive operation.
     * <p>
     * An alternative approach may be to use a regular expression to check for validity of being a number:
     *
     * @param str
     * @return true if is Number
     */
    public boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }

        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
