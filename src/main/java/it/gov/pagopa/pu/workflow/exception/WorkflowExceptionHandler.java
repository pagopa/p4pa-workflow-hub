package it.gov.pagopa.pu.workflow.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.exception.custom.InvalidWfExecutionConfigException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * A class exception that handles errors related to workflows.
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowExceptionHandler {

  @ExceptionHandler(WorkflowExecutionAlreadyStarted.class)
  public ResponseEntity<WorkflowErrorDTO> handleWorkflowExecutionAlreadyStarted(WorkflowExecutionAlreadyStarted ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CodeEnum.CONFLICT);
  }

  @ExceptionHandler(value = {IngestionFlowTypeNotSupportedException.class, InvalidWfExecutionConfigException.class})
  public ResponseEntity<WorkflowErrorDTO> handleIngestionFlowTypeNotSupportedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CodeEnum.INGESTION_FLOW_FILE_NOT_SUPPORTED);
  }

  @ExceptionHandler({WorkflowNotFoundException.class})
  public ResponseEntity<WorkflowErrorDTO> handleNotFoundWorkflowError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, WorkflowErrorDTO.CodeEnum.NOT_FOUND);
  }

  @ExceptionHandler({WorkflowInternalErrorException.class})
  public ResponseEntity<WorkflowErrorDTO> handleInternalError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CodeEnum.GENERIC_ERROR);
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<WorkflowErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CodeEnum.BAD_REQUEST);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<WorkflowErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatusCode httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    WorkflowErrorDTO.CodeEnum errorCode = WorkflowErrorDTO.CodeEnum.GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = errorResponse.getStatusCode();
      if (httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = WorkflowErrorDTO.CodeEnum.NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = WorkflowErrorDTO.CodeEnum.BAD_REQUEST;
      }
    }
    return handleException(ex, request, httpStatus, errorCode);
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<WorkflowErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CodeEnum.GENERIC_ERROR);
  }

  static ResponseEntity<WorkflowErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus, WorkflowErrorDTO.CodeEnum errorEnum) {
    logException(ex, request, httpStatus);

    String message = buildReturnedMessage(ex);

    return ResponseEntity
      .status(httpStatus)
      .body(new WorkflowErrorDTO(errorEnum, message));
  }

  private static void logException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus) {
    boolean printStackTrace = httpStatus.is5xxServerError();
    Level logLevel = printStackTrace ? Level.ERROR : Level.INFO;
    log.makeLoggingEventBuilder(logLevel)
      .log("A {} occurred handling request {}: HttpStatus {} - {}",
        ex.getClass(),
        getRequestDetails(request),
        httpStatus.value(),
        ex.getMessage(),
        printStackTrace ? ex : null
      );
    if (!printStackTrace && log.isDebugEnabled() && ex.getCause() != null) {
      log.debug("CausedBy: ", ex.getCause());
    }
  }

  private static String buildReturnedMessage(Exception ex) {
    if (ex instanceof HttpMessageNotReadableException) {
      if (ex.getCause() instanceof JsonMappingException jsonMappingException) {
        return "Cannot parse body: " +
          jsonMappingException.getPath().stream()
            .map(JsonMappingException.Reference::getFieldName)
            .collect(Collectors.joining(".")) +
          ": " + jsonMappingException.getOriginalMessage();
      }
      return "Required request body is missing";
    } else if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
      return "Invalid request content:" +
        methodArgumentNotValidException.getBindingResult()
          .getAllErrors().stream()
          .map(e -> " " +
            (e instanceof FieldError fieldError ? fieldError.getField() : e.getObjectName()) +
            ": " + e.getDefaultMessage())
          .sorted()
          .collect(Collectors.joining(";"));
    } else {
      return ex.getMessage();
    }
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }
}
