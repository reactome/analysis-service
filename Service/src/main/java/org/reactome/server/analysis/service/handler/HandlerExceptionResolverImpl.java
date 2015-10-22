package org.reactome.server.analysis.service.handler;

import org.apache.log4j.Logger;
import org.reactome.server.analysis.service.exception.AnalysisServiceException;
import org.reactome.server.analysis.service.model.AnalysisError;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * By implementing the HandlerExceptionResolver we can check for different exceptions happened
 * during handler mapping or execution. Different actions can be developed depending of the
 * exception type.
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class HandlerExceptionResolverImpl implements HandlerExceptionResolver {

    private static Logger logger = Logger.getLogger(HandlerExceptionResolverImpl.class.getName());

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {

        response.setContentType("application/json");

        if (ex instanceof MaxUploadSizeExceededException) {
            response.setStatus(HttpStatus.REQUEST_ENTITY_TOO_LARGE.value());

            Long maxSizeInBytes = ((MaxUploadSizeExceededException) ex).getMaxUploadSize();
            Integer mb = (int) Math.floor(maxSizeInBytes / 1024 / 1024);

            AnalysisError error = new AnalysisError(
                    HttpStatus.REQUEST_ENTITY_TOO_LARGE,
                    "Maximum upload size of " + mb + " MB per attachment exceeded"
            );
            try {
                response.getWriter().println(error);
            } catch (IOException e) {
                logger.error("Error writing to output stream", e);
            }
            return new ModelAndView();
        }

        if (ex instanceof MultipartException) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            AnalysisError error = new AnalysisError(HttpStatus.BAD_REQUEST, ex.getMessage());
            try {
                response.getWriter().println(error);
            } catch (IOException e) {
                logger.error("Error writing to output stream", e);
            }
            return new ModelAndView();
        }

        if (ex instanceof AnalysisServiceException) {
            AnalysisServiceException ase = (AnalysisServiceException) ex;

            response.setStatus(ase.getHttpStatus().value());
            AnalysisError error = new AnalysisError(ase);
            try {
                response.getWriter().println(error);
            } catch (IOException e) {
                logger.error("Error writing to output stream", e);
            }
            return new ModelAndView();
        }

        ex.printStackTrace();

        return null;
    }
}