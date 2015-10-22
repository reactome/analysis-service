package org.reactome.server.analysis.parser.exception;

import java.util.List;

/**
 * @author gsviteri
 */
public class ParserException extends Exception {

    private List<String> errorMessages;

    public ParserException(String message, List<String> errorMessages) {
        super(message);
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
