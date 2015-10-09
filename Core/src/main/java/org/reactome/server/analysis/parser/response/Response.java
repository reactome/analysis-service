package org.reactome.server.analysis.parser.response;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that builds the Response.
 * Created by gsviteri on 09/10/2015.
 */
public class Response {

    /**
     * Codes
     */
    public final static Integer UNEXPECTED_ERROR = new Integer(450);

    public final static Integer NO_HEADER = new Integer(451);
    public final static Integer EMPTY_FILE = new Integer(452);
    public final static Integer MALFORMED_HEADER = new Integer(453);
    public final static Integer INLINE_PROBLEM = new Integer(454);
    public final static Integer EMPTY_LINE = new Integer(455);
    public final static Integer POTENTIAL_HEADER = new Integer(456);
    public final static Integer COLUMN_MISMATCH = new Integer(457);

    /**
     * messages - Strings
     */
    private final static String MESSAGE_UNEXPECTED_ERROR = "Unexpected Error";
    private final static String MESSAGE_NO_HEADER = "Missing header. Using a default one.";
    private final static String MESSAGE_EMPTY_FILE = "There is no file to be analysed.";
    private final static String MESSAGE_MALFORMED_HEADER = "Malformed header.";
    private final static String MESSAGE_INLINE_PROBLEM = "Line {0} has been removed. Invalid value found on Column {1}.";
    private final static String MESSAGE_EMPTY_LINE = "The line {0} is empty and has been ignored.";
    private final static String MESSAGE_POTENTIAL_HEADER = "The first line seems to be a header. Make sure it is being initialized by # or //.";
    private final static String MESSAGE_COLUMN_MISMATCH = "Line {0} does not have {1} column(s). {2} Column(s) found.";

    /**
     * handles Error codes to Message strings
     */
    public final static Map<Integer, String> codeToMessage;

    static {
        codeToMessage = new HashMap<Integer, String>(8);

        codeToMessage.put(UNEXPECTED_ERROR, MESSAGE_UNEXPECTED_ERROR);

        codeToMessage.put(NO_HEADER, MESSAGE_NO_HEADER);
        codeToMessage.put(EMPTY_FILE, MESSAGE_EMPTY_FILE);
        codeToMessage.put(MALFORMED_HEADER, MESSAGE_MALFORMED_HEADER);
        codeToMessage.put(INLINE_PROBLEM, MESSAGE_INLINE_PROBLEM);
        codeToMessage.put(EMPTY_LINE, MESSAGE_EMPTY_LINE);
        codeToMessage.put(POTENTIAL_HEADER, MESSAGE_POTENTIAL_HEADER);
        codeToMessage.put(COLUMN_MISMATCH, MESSAGE_COLUMN_MISMATCH);

    }

    /**
     * Return appropriate message based on the code
     * @param code
     * @return
     */
    public static String getMessage(Integer code) {
        String message = (String) codeToMessage.get(code);
        return message;
    }


    /**
     * Returns error message based on param int code after formatting message
     * with args
     * @param code
     * @param args
     * @return
     */
    public static String getMessage(Integer code, Object... args) {

        String message = Response.getMessage(code);

        if ((args != null) && (args.length > 0)) {
            MessageFormat format = new MessageFormat(message);
            message = format.format(args);
        }

        return message;
    }

    /**
     * Returns error message based on param int code after formatting message
     * with arg
     * @param code
     * @param arg
     * @return
     */
    public static String getMessage(Integer code, int arg) {

        String message = Response.getMessage(code);

        MessageFormat format = new MessageFormat(message);
        message = format.format(new Object[] { new Integer(arg) });

        return message;
    }
}
