package it.gov.pagopa.pu.workflow.exception;

import io.temporal.client.WorkflowExecutionAlreadyStarted;
import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.exception.common.CommonExceptionHandler;
import it.gov.pagopa.pu.workflow.exception.custom.*;
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
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowExceptionHandler extends CommonExceptionHandler {

  @ExceptionHandler(WorkflowExecutionAlreadyStarted.class)
  public ResponseEntity<WorkflowErrorDTO> handleWorkflowExecutionAlreadyStarted(WorkflowExecutionAlreadyStarted ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT);
  }

  @ExceptionHandler(value = IngestionFlowTypeNotSupportedException.class)
  public ResponseEntity<WorkflowErrorDTO> handleIngestionFlowTypeNotSupportedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CategoryEnum.WORKFLOW_INGESTION_FLOW_FILE_NOT_SUPPORTED);
  }

  @ExceptionHandler(value = InvalidWfExecutionConfigException.class)
  public ResponseEntity<WorkflowErrorDTO> handleInvalidWfExecutionConfigException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CategoryEnum.WORKFLOW_INVALID_SYNC_DP_WF_EXECUTION_CONFIG);
  }

  @ExceptionHandler({WorkflowNotFoundException.class, WorkflowTypeNotFoundException.class, io.temporal.client.WorkflowNotFoundException.class})
  public ResponseEntity<WorkflowErrorDTO> handleWfNotFoundException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, WorkflowErrorDTO.CategoryEnum.WORKFLOW_NOT_FOUND);
  }

  @ExceptionHandler({WorkflowConflictException.class})
  public ResponseEntity<WorkflowErrorDTO> handleWfConflictException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT);
  }

  @ExceptionHandler({WorkflowInternalErrorException.class})
  public ResponseEntity<WorkflowErrorDTO> handleWfInternalError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CategoryEnum.WORKFLOW_GENERIC_ERROR);
  }

  @ExceptionHandler({TooManyAttemptsException.class})
  public ResponseEntity<WorkflowErrorDTO> handleWfTooManyAttemptsException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.REQUEST_TIMEOUT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_REQUEST_TIMEOUT);
  }

}
