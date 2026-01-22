package it.gov.pagopa.pu.workflow.exception;

import io.temporal.client.WorkflowExecutionAlreadyStarted;
import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.exception.custom.*;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import jakarta.persistence.RollbackException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class exception that handles errors related to workflows.
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowExceptionHandler {

  private static final String ERROR_MESSAGE_FORMAT = "[%s] %s";

  @ExceptionHandler(WorkflowExecutionAlreadyStarted.class)
  public ResponseEntity<WorkflowErrorDTO> handleWorkflowExecutionAlreadyStarted(WorkflowExecutionAlreadyStarted ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CodeEnum.WORKFLOW_CONFLICT);
  }

  @ExceptionHandler(value = IngestionFlowTypeNotSupportedException.class)
  public ResponseEntity<WorkflowErrorDTO> handleIngestionFlowTypeNotSupportedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CodeEnum.WORKFLOW_INGESTION_FLOW_FILE_NOT_SUPPORTED);
  }

  @ExceptionHandler(value = InvalidWfExecutionConfigException.class)
  public ResponseEntity<WorkflowErrorDTO> handleInvalidWfExecutionConfigException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CodeEnum.WORKFLOW_INVALID_SYNC_DP_WF_EXECUTION_CONFIG);
  }

  @ExceptionHandler({WorkflowNotFoundException.class, WorkflowTypeNotFoundException.class, ResourceNotFoundException.class, io.temporal.client.WorkflowNotFoundException.class})
  public ResponseEntity<WorkflowErrorDTO> handleNotFoundException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, WorkflowErrorDTO.CodeEnum.WORKFLOW_NOT_FOUND);
  }

  @ExceptionHandler({WorkflowInternalErrorException.class})
  public ResponseEntity<WorkflowErrorDTO> handleInternalError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CodeEnum.WORKFLOW_GENERIC_ERROR);
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<WorkflowErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CodeEnum.WORKFLOW_BAD_REQUEST);
  }

  @ExceptionHandler({TooManyAttemptsException.class})
  public ResponseEntity<WorkflowErrorDTO> handleTooManyAttemptsException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.REQUEST_TIMEOUT, WorkflowErrorDTO.CodeEnum.WORKFLOW_REQUEST_TIMEOUT);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<WorkflowErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    WorkflowErrorDTO.CodeEnum errorCode = WorkflowErrorDTO.CodeEnum.WORKFLOW_GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = HttpStatus.valueOf((errorResponse.getStatusCode().value()));
      if (httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = WorkflowErrorDTO.CodeEnum.WORKFLOW_NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = WorkflowErrorDTO.CodeEnum.WORKFLOW_BAD_REQUEST;
      }
    }
    return handleException(ex, request, httpStatus, errorCode);
  }

  @ExceptionHandler({TransactionException.class})
  public ResponseEntity<WorkflowErrorDTO> handleTransactionException(TransactionException ex, HttpServletRequest request) {
    if (ex.getCause() instanceof RollbackException rollbackException && rollbackException.getCause() instanceof ValidationException validationException) {
      return handleViolationException(validationException, request);
    } else {
      return handleRuntimeException(ex, request);
    }
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<WorkflowErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CodeEnum.WORKFLOW_GENERIC_ERROR);
  }

  static ResponseEntity<WorkflowErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatus httpStatus, WorkflowErrorDTO.CodeEnum errorEnum) {
    logException(ex, request, httpStatus);

    String message = Optional.of(request.getRequestURI())
      .filter(path -> path.contains("/crud/"))
      .map(path -> buildCrudErrorMessage(path, httpStatus, ex))
      .orElseGet(() -> buildReturnedMessage(ex));

    return ResponseEntity
      .status(httpStatus)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new WorkflowErrorDTO(errorEnum, message, Utilities.getTraceId()));
  }

  private static String buildCrudErrorMessage(String requestPath, HttpStatus httpStatus, Exception ex) {
    String entity = requestPath.split("/crud/")[1].split("/")[0].replaceAll("s$", "");
    String entityCode = entity.replace("-", "_").toUpperCase();
    return String.format(ERROR_MESSAGE_FORMAT, entityCode + "_" + httpStatus.name(), ex.getMessage());
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
    switch (ex) {
      case HttpMessageNotReadableException httpMessageNotReadableException -> {
        if (httpMessageNotReadableException.getCause() instanceof DatabindException jsonMappingException) {
          return String.format(ERROR_MESSAGE_FORMAT, WorkflowErrorDTO.CodeEnum.WORKFLOW_BAD_REQUEST.name(),
            "Cannot parse body. " +
            jsonMappingException.getPath().stream()
              .map(JacksonException.Reference::getPropertyName)
              .collect(Collectors.joining(".")) +
            ": " + jsonMappingException.getOriginalMessage());
        }
        return String.format(ERROR_MESSAGE_FORMAT, WorkflowErrorDTO.CodeEnum.WORKFLOW_BAD_REQUEST.name(),
          "Required request body is missing");
      }
      case MethodArgumentNotValidException methodArgumentNotValidException -> {
        return String.format(ERROR_MESSAGE_FORMAT, WorkflowErrorDTO.CodeEnum.WORKFLOW_BAD_REQUEST.name(),
          "Invalid request content." +
          methodArgumentNotValidException.getBindingResult()
            .getAllErrors().stream()
            .map(e -> " " +
              (e instanceof FieldError fieldError ? fieldError.getField() : e.getObjectName()) +
              ": " + e.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining(";")));
      }
      case ConstraintViolationException constraintViolationException -> {
        return String.format(ERROR_MESSAGE_FORMAT, WorkflowErrorDTO.CodeEnum.WORKFLOW_BAD_REQUEST.name(),
          "Invalid request content." +
          constraintViolationException.getConstraintViolations()
            .stream()
            .map(e -> " " + e.getPropertyPath() + ": " + e.getMessage())
            .sorted()
            .collect(Collectors.joining(";")));
      }
      default -> {
        return ex.getMessage();
      }
    }
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }
}
