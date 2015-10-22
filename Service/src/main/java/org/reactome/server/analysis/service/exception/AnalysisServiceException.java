package org.reactome.server.analysis.service.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class AnalysisServiceException extends RuntimeException {

    private HttpStatus httpStatus;
    private List<String> errorMessages;

    AnalysisServiceException(HttpStatus httpStatus) {
        super(httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
    }

    public AnalysisServiceException(HttpStatus httpStatus, String message) {
        super(httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
        this.errorMessages = new LinkedList<>();
        this.errorMessages.add(message);
    }

    public AnalysisServiceException(HttpStatus httpStatus, List<String> errorMessages) {
        super(httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
        this.errorMessages = new LinkedList<>();
        this.errorMessages = errorMessages;
    }


    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
