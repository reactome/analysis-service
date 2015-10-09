package org.reactome.server.analysis.parser.exception;

import java.util.List;

/**
 * Created by gsviteri on 09/10/2015.
 */
public class ParserException extends Exception {

    private String message;
    private List<String> errorMessage;

    public ParserException(String message, List<String> errorMessage){
        super(message);
        this.message = message;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }
}
