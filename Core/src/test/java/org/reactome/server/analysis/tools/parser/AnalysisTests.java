package org.reactome.server.analysis.tools.parser;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.reactome.server.analysis.parser.InputFormat;
import org.reactome.server.analysis.parser.exception.ParserException;

import java.io.*;
import java.net.URL;

/**
 * Tester class for the Analysis parser
 *
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 */

public class AnalysisTests {

    private static final String PATH = "analysis/input/";

    /**
     * MULTIPLE LINES INPUTS
     **/
    private static final String CARRIAGE_RETURN = PATH.concat("carriage_return.txt");
    private static final String CARRIAGE_RETURN_ERROR = PATH.concat("carriage_return_error.txt");
    private static final String CARRIAGE_RETURN_WARNING = PATH.concat("carriage_return_warning.txt");
    private static final String EMPTY_FILE = PATH.concat("empty_file.txt");
    private static final String EMPTY_FILE_SPACES = PATH.concat("empty_file_with_spaces.txt");
    private static final String EMPTY_LINES = PATH.concat("empty_lines.txt");
    private static final String EMPTY_LINES_WITH_SPACES = PATH.concat("empty_lines_with_spaces.txt");
    private static final String INLINE_PROBLEMS = PATH.concat("inline_problems.txt");
    private static final String MISSING_HEADER = PATH.concat("missing_header.txt");
    private static final String POTENTIAL_HEADER = PATH.concat("potential_header.txt");
    private static final String SPACES_ON_HEADER = PATH.concat("spaces_on_header.txt");
    private static final String CORRECT_FILE = PATH.concat("correct_file.txt");
    private static final String COLUMN_MISMATCH_HEADER = PATH.concat("column_mismatch_header.txt");
    private static final String COLUMN_MISMATCH_CONTENT = PATH.concat("column_mismatch_content.txt");
    private static final String MULTIPLE_WARNINGS = PATH.concat("multiple_warnings.txt");
    private static final String BROKEN_FILE = PATH.concat("broken_file.txt");

    /**
     * SINGLE LINE INPUTS
     **/
    private static final String ONELINE_START_WITH_NUMBER = PATH.concat("oneline_start_with_number.txt");
    private static final String ONELINE_START_WITH_HASH = PATH.concat("oneline_start_with_hash.txt");
    private static final String ONELINE_IDENTIFIERS = PATH.concat("oneline_identifiers.txt");
    private static final String ONELINE_ID_EXPRESSIONS = PATH.concat("oneline_id_expressions.txt");
    private static final String ONELINE_ID_EXPRESSIONS_MIXED = PATH.concat("oneline_id_expressions_mixed.txt");
    private static final String ONELINE_CSV = PATH.concat("oneline_csv.txt");
    private static final String ONELINE_OTHERSSEPARATOR = PATH.concat("oneline_others_separators.txt");
    private static final String ONELINE_START_WITH_SPACES = PATH.concat("oneline_start_with_spaces.txt");
    private static final String ONELINE_BUT_NOT_IN_FIRST_LINE = PATH.concat("oneline_but_not_in_first_line.txt");
    private static final String ONELINE_RANDOM_CHARACTERS = PATH.concat("oneline_random_characters.txt");

    @Test
    public void testEmptyLines() {
        File file = getFileFromResources(EMPTY_LINES);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(EMPTY_FILE + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(14, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(8, format.getWarningResponses().size());

    }

    @Test
    public void testEmptyLinesWithSpaces() {
        File file = getFileFromResources(EMPTY_LINES_WITH_SPACES);
        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(EMPTY_LINES_WITH_SPACES + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(14, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(7, format.getWarningResponses().size());

    }

    @Test
    public void testInlineProblems() {
        File file = getFileFromResources(INLINE_PROBLEMS);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(INLINE_PROBLEMS + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(10, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(9, format.getWarningResponses().size());
    }

    @Test
    public void testEmptyFile() {
        File file = getFileFromResources(EMPTY_FILE);

        try {
            parser(file);

            Assert.fail(EMPTY_FILE + " should fail");

        } catch (ParserException e) {
            Assert.assertTrue(e.getErrorMessages().contains("There is no file to be analysed."));
        }
    }


    @Test
    public void testEmptyFileWithSpaces() {
        File file = getFileFromResources(EMPTY_FILE_SPACES);

        try {
            parser(file);

            Assert.fail(EMPTY_FILE_SPACES + " should fail.");
        } catch (ParserException e) {
            Assert.assertTrue(e.getErrorMessages().contains("There is no file to be analysed."));
        }
    }

    @Test
    public void testMissingHeader() {
        File file = getFileFromResources(MISSING_HEADER);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(MISSING_HEADER + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(9, format.getAnalysisIdentifierSet().size());
        Assert.assertTrue("Missing header was expected", format.getWarningResponses().contains("Missing header. Using a default one."));
        Assert.assertEquals(1, format.getWarningResponses().size());

    }

    @Test
    public void testPotentialHeader() {
        File file = getFileFromResources(POTENTIAL_HEADER);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(POTENTIAL_HEADER + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(9, format.getAnalysisIdentifierSet().size());
        Assert.assertTrue("First line does not match the expected result.", format.getWarningResponses().contains("The first line seems to be a header. Make sure it is being initialised by # or //."));
        Assert.assertEquals(1, format.getWarningResponses().size());

    }

    @Test
    public void testSpacesOnHeader() {
        File file = getFileFromResources(SPACES_ON_HEADER);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(SPACES_ON_HEADER + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertTrue("Header does not match",format.getHeaderColumnNames().contains("10h After"));
        Assert.assertEquals(9, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testCorrectFile() {
        File file = getFileFromResources(CORRECT_FILE);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(CORRECT_FILE + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(9, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testColumnMismatchHeader() {
        File file = getFileFromResources(COLUMN_MISMATCH_HEADER);

        try {
            parser(file);

            Assert.fail(COLUMN_MISMATCH_HEADER + " should fail");
        } catch (ParserException e) {
            Assert.assertEquals(9, e.getErrorMessages().size());
        }

    }

    @Test
    public void testColumnMismatchContent() {
        File file = getFileFromResources(COLUMN_MISMATCH_CONTENT);

        try {
            parser(file);
            Assert.fail(COLUMN_MISMATCH_HEADER + " should fail");

        } catch (ParserException e) {
            Assert.assertEquals(6, e.getErrorMessages().size());
        }
    }

    @Test
    public void testCarriageReturn() {
        File file = getFileFromResources(CARRIAGE_RETURN);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(CARRIAGE_RETURN + " has failed.");
        }

        Assert.assertEquals(5, format.getHeaderColumnNames().size());
        Assert.assertEquals(77, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testMultipleWarnings() {
        File file = getFileFromResources(MULTIPLE_WARNINGS);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(MULTIPLE_WARNINGS + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(6, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(35, format.getWarningResponses().size());

        int emptyLines = 0;
        for (String warn : format.getWarningResponses()){
            if(warn.contains("is empty and has been ignored")){
                emptyLines++;
            }
        }

        Assert.assertEquals(30, emptyLines);

    }

    @Test
    public void testCarriageReturnErrors() {
        File file = getFileFromResources(CARRIAGE_RETURN_ERROR);

        try {
            parser(file);
            Assert.fail(CARRIAGE_RETURN_ERROR + " should fail.");
        } catch (ParserException e) {
            Assert.assertEquals(2, e.getErrorMessages().size());
        }

    }

    @Test
    public void testCarriageReturnWarnings() {
        File file = getFileFromResources(CARRIAGE_RETURN_WARNING);
        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(CARRIAGE_RETURN_WARNING + " has failed.");
        }

        Assert.assertEquals(5, format.getHeaderColumnNames().size());
        Assert.assertEquals(77, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(3, format.getWarningResponses().size());

    }

    /**
     * ONE LINE TESTS
     **/

    @Test
    public void testOneLineStartingWithNumber() {
        File file = getFileFromResources(ONELINE_START_WITH_NUMBER);

        try {
            parser(file);

            Assert.fail(ONELINE_START_WITH_NUMBER + " should fail");
        } catch (ParserException e) {
            Assert.assertTrue("Expecting start with number", e.getErrorMessages().contains("A single line input cannot start with number."));
        }

    }

    @Test
    public void testOneLineStartingWithHash() {
        File file = getFileFromResources(ONELINE_START_WITH_HASH);

        try {
            parser(file);

            Assert.fail(ONELINE_START_WITH_HASH + " has failed.");
        } catch (ParserException e) {
            Assert.assertTrue("Expecting start with comment", e.getErrorMessages().contains("A single line input cannot start with hash or comment."));
        }
    }

    @Test
    public void testOneLineIdentifiers() { //success
        File file = getFileFromResources(ONELINE_IDENTIFIERS);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(ONELINE_IDENTIFIERS + " has failed.");
        }

        Assert.assertEquals(1, format.getHeaderColumnNames().size());
        Assert.assertEquals(6, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testOneLineIdAndExpressions() { //success
        File file = getFileFromResources(ONELINE_ID_EXPRESSIONS);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(ONELINE_ID_EXPRESSIONS + " has failed.");
        }

        Assert.assertEquals(6, format.getHeaderColumnNames().size());
        Assert.assertEquals(1, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testOneLineIdAndExpressionsMixed() {
        File file = getFileFromResources(ONELINE_ID_EXPRESSIONS_MIXED);

        try {
            parser(file);

            Assert.fail(ONELINE_ID_EXPRESSIONS_MIXED + " should fail.");
        } catch (ParserException e) {
            Assert.assertTrue(e.getErrorMessages().contains("A single line input is invalid. Found proteins and values together."));
        }
    }

    @Test
    public void testOneLineCsv() {
        File file = getFileFromResources(ONELINE_CSV);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(ONELINE_CSV + " has failed.");
        }

        Assert.assertEquals(1, format.getHeaderColumnNames().size());
        Assert.assertEquals(6, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testOneLineOthersSeparator() {
        File file = getFileFromResources(ONELINE_OTHERSSEPARATOR);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(ONELINE_OTHERSSEPARATOR + " has failed.");
        }

        Assert.assertEquals(1, format.getHeaderColumnNames().size());
        Assert.assertEquals(7, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testOneLineStartWithSpaces() {
        File file = getFileFromResources(ONELINE_START_WITH_SPACES);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(ONELINE_START_WITH_SPACES + " has failed.");
        }

        Assert.assertEquals(1, format.getHeaderColumnNames().size());
        Assert.assertEquals(6, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testOneLineButNotInFirstLine() {
        File file = getFileFromResources(ONELINE_BUT_NOT_IN_FIRST_LINE);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(ONELINE_BUT_NOT_IN_FIRST_LINE + " has failed.");
        }

        Assert.assertTrue(format.getHeaderColumnNames().size() == 1);
        Assert.assertTrue(format.getAnalysisIdentifierSet().size() == 6);
        Assert.assertTrue(format.getWarningResponses().size() == 0);

    }

    @Test
    public void testOneLineRandomCharacters() {
        File file = getFileFromResources(ONELINE_RANDOM_CHARACTERS);

        InputFormat format = null;
        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail(ONELINE_RANDOM_CHARACTERS + " has failed.");
        }

        Assert.assertTrue(format.getHeaderColumnNames().size() == 1);
        Assert.assertTrue(format.getAnalysisIdentifierSet().size() == 4);
        Assert.assertTrue(format.getWarningResponses().size() == 0);

    }

    @Test
    public void testBrokenFile() {
        File file = getFileFromResources(BROKEN_FILE);

        try {
            parser(file);

            Assert.fail(BROKEN_FILE + " should fail.");
        } catch (ParserException e) {
            Assert.assertEquals(5, e.getErrorMessages().size());
        }

    }

    @Test
    public void testMaxFileSize() {
        File file = writeHugeFile();

        InputFormat format = null;

        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail("10MB file has failed.");
        } finally {
            file.deleteOnExit();
        }

        Assert.assertEquals(4, format.getHeaderColumnNames().size());
        Assert.assertEquals(310000, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());

    }

    @Test
    public void testOnelineHuge() {
        File file = writeOneLineHugeFile();

        InputFormat format = null;

        try {
            format = parser(file);
        } catch (ParserException e) {
            Assert.fail("10MB one line file has failed.");
        } finally {
            file.deleteOnExit();
        }

        Assert.assertEquals(1, format.getHeaderColumnNames().size());
        Assert.assertEquals(750000, format.getAnalysisIdentifierSet().size());
        Assert.assertEquals(0, format.getWarningResponses().size());


    }
    private InputFormat parser(File file) throws ParserException {
        InputFormat format = new InputFormat();

        try {
            InputStream fis = new FileInputStream(file);
            format.parseData(IOUtils.toString(fis));
        } catch (IOException e) {
            Assert.fail("Couldn't get the file to be analysed properly. File [".concat(file.getName()).concat("]"));
        }

        return format;
    }

    private File getFileFromResources(String fileName) {
        String msg = "Can't get an instance of ".concat(fileName);

        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader == null) {
            Assert.fail("[1] - ".concat(msg));
        }

        URL url = classLoader.getResource(fileName);
        if (url == null) {
            Assert.fail("[2] - ".concat(msg));
        }

        File file = new File(url.getFile());
        if (!file.exists()) {
            Assert.fail("[3] - ".concat(msg));
        }

        return file;
    }

    /**
     * This is a 10MB files, we do not want to store a huge file in GitHub
     * If you want to test a 10mb file, please remove @Ignore in the method
     */
    private File writeHugeFile(){

        File file = new File("max_file_size.txt");

        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write("#Probeset\t");
            bw.write("Column 1\t");
            bw.write("Column 2\t");
            bw.write("Column 3");
            bw.newLine();

            for (int i = 0; i < 310000; i++) {
                bw.write(i + "_abcd\t");

                bw.write("5.1234\t");
                bw.write("4.1234\t");
                bw.write("3.1234");
                bw.newLine();

                if(i % 10000 == 0){
                    bw.flush();
                }
            }

            bw.flush();

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return file;
    }

    /**
     * This is a 10MB files, we do not want to store a huge file in GitHub
     * If you want to test a 10mb file, please remove @Ignore in the method
     */
    private File writeOneLineHugeFile(){

        File file = new File("one_line_max_file_size.txt");

        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            for (int i = 0; i < 750000; i++) {
                bw.write(i + "_abcdef\t");

                if(i % 10000 == 0){
                    bw.flush();
                }
            }

            bw.flush();

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return file;
    }
}

