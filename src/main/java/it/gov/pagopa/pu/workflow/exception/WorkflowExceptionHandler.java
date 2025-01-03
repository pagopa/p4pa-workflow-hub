package it.gov.pagopa.pu.workflow.exception;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A class exception that handles errors related to workflows.
 *
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowExceptionHandler {

  @ExceptionHandler({WorkflowNotFoundException.class})
  public ResponseEntity<WorkflowErrorDTO> handleNotFoundWorkflowError(RuntimeException ex, HttpServletRequest request){
    return handleWorkflowErrorException(ex, request, HttpStatus.NOT_FOUND, WorkflowErrorDTO.CodeEnum.NOT_FOUND);
  }

  @ExceptionHandler({WorkflowInternalErrorException.class})
  public ResponseEntity<WorkflowErrorDTO> handleInternalError(RuntimeException ex, HttpServletRequest request){
    return handleWorkflowErrorException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CodeEnum.GENERIC_ERROR);
  }

  static ResponseEntity<WorkflowErrorDTO> handleWorkflowErrorException(RuntimeException ex, HttpServletRequest request, HttpStatus httpStatus, WorkflowErrorDTO.CodeEnum errorEnum) {
    String message = logException(ex, request, httpStatus);

    return ResponseEntity
      .status(httpStatus)
      .body(new WorkflowErrorDTO(errorEnum, message));
  }

  private static String logException(RuntimeException ex, HttpServletRequest request, HttpStatus httpStatus) {
    String message = ex.getMessage();
    log.info("A {} occurred handling request {}: HttpStatus {} - {}",
      ex.getClass(),
      getRequestDetails(request),
      httpStatus.value(),
      message);
    return message;
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }
}
