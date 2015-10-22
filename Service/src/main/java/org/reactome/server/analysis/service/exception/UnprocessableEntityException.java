package org.reactome.server.analysis.service.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
//Do NOT annotate it with "ResponseStatus" because it is treated in "HandlerExceptionResolverImpl"
public final class UnprocessableEntityException extends AnalysisServiceException {

    public UnprocessableEntityException() {
        super(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
