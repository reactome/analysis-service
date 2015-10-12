package org.reactome.server.analysis.parser.exception;

import java.util.List;

/**
 * Created by gsviteri on 09/10/2015.
 * @author gsviteri
 */
public class ParserException extends Exception {

    private List<String> errorMessage;

    public ParserException(String message, List<String> errorMessage){
        super(message);
        this.errorMessage = errorMessage;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }
}
