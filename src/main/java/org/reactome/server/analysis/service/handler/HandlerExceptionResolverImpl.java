package org.reactome.server.analysis.service.handler;

import org.reactome.server.analysis.core.result.exception.AnalysisServiceException;
import org.reactome.server.analysis.core.result.model.AnalysisError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
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
@Component
public class HandlerExceptionResolverImpl implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger("resultLogger");

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {

        response.setContentType("application/json");

        if (ex instanceof MaxUploadSizeExceededException) {
            response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());

            long maxSizeInBytes = ((MaxUploadSizeExceededException) ex).getMaxUploadSize();
            int mb = (int) Math.floor(maxSizeInBytes / 1024 / 1024);

            AnalysisError error = new AnalysisError(
                    HttpStatus.PAYLOAD_TOO_LARGE,
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

        response.setStatus(500);
        AnalysisError error = new AnalysisError(HttpStatus.INTERNAL_SERVER_ERROR, "Please get in touch with our help desk (help@reactome.org) for more details about this problem");
        try {
            response.getWriter().println(error);
        } catch (IOException e) {
            logger.error("Error writing to output stream", e);
        } catch (IllegalStateException ise){
            logger.error("Connection stream was closed by the user");
        }
        return new ModelAndView();
    }
}