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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
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

import java.util.Objects;
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

  @ExceptionHandler({WorkflowNotFoundException.class, WorkflowTypeNotFoundException.class, ResourceNotFoundException.class, io.temporal.client.WorkflowNotFoundException.class})
  public ResponseEntity<WorkflowErrorDTO> handleNotFoundException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, WorkflowErrorDTO.CategoryEnum.WORKFLOW_NOT_FOUND);
  }

  @ExceptionHandler({WorkflowConflictException.class})
  public ResponseEntity<WorkflowErrorDTO> handleConflictException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT);
  }

  @ExceptionHandler({WorkflowInternalErrorException.class})
  public ResponseEntity<WorkflowErrorDTO> handleInternalError(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CategoryEnum.WORKFLOW_GENERIC_ERROR);
  }

  @ExceptionHandler({DataIntegrityViolationException.class})
  public ResponseEntity<WorkflowErrorDTO> handleDataIntegrityViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT);
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class, InvalidValueException.class})
  public ResponseEntity<WorkflowErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST);
  }

  @ExceptionHandler({TooManyAttemptsException.class})
  public ResponseEntity<WorkflowErrorDTO> handleTooManyAttemptsException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.REQUEST_TIMEOUT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_REQUEST_TIMEOUT);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<WorkflowErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    WorkflowErrorDTO.CategoryEnum errorCode = WorkflowErrorDTO.CategoryEnum.WORKFLOW_GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = HttpStatus.valueOf((errorResponse.getStatusCode().value()));
      if (httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = WorkflowErrorDTO.CategoryEnum.WORKFLOW_NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST;
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
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CategoryEnum.WORKFLOW_GENERIC_ERROR);
  }

  static ResponseEntity<WorkflowErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatus httpStatus, WorkflowErrorDTO.CategoryEnum errorEnum) {
    logException(ex, request, httpStatus);

    Pair<String, String> code2message = Optional.of(request.getRequestURI())
      .filter(path -> path.contains("/crud/"))
      .map(path -> buildCrudErrorMessage(path, httpStatus, ex))
      .orElseGet(() -> buildReturnedMessage(ex));

    String code = Objects.requireNonNullElse(code2message.getLeft(), errorEnum.getValue());
    String message = code2message.getRight();

    return ResponseEntity
      .status(httpStatus)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new WorkflowErrorDTO(errorEnum, code, String.format(ERROR_MESSAGE_FORMAT, code, message), Utilities.getTraceId()));
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

  private static Pair<String, String> buildCrudErrorMessage(String requestPath, HttpStatus httpStatus, Exception ex) {
    String entity = requestPath.split("/crud/")[1].split("/")[0].replaceAll("s$", "");
    String entityCode = entity.replace("-", "_").toUpperCase();
    return Pair.of(entityCode + "_" + httpStatus.name(), buildReturnedMessage(ex).getValue());
  }

  private static Pair<String, String> buildReturnedMessage(Exception ex) {
    switch (ex) {
      case HttpMessageNotReadableException httpMessageNotReadableException -> {
        String errorMsg = "Required request body is missing";
        if (httpMessageNotReadableException.getCause() instanceof DatabindException jsonMappingException) {
          errorMsg = "Cannot parse body. " +
            jsonMappingException.getPath().stream()
              .map(JacksonException.Reference::getPropertyName)
              .collect(Collectors.joining(".")) +
            ": " + jsonMappingException.getOriginalMessage();
        } else if (httpMessageNotReadableException.getCause() instanceof JacksonException jacksonException) {
          errorMsg = "Cannot parse body. " + jacksonException.getOriginalMessage();
        }
        return Pair.of(WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST.name(), errorMsg);
      }
      case MethodArgumentNotValidException methodArgumentNotValidException -> {
        return Pair.of(WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST.name(),
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
        return Pair.of(WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST.name(),
          "Invalid request content." +
          constraintViolationException.getConstraintViolations()
            .stream()
            .map(e -> " " + e.getPropertyPath() + ": " + e.getMessage())
            .sorted()
            .collect(Collectors.joining(";")));
      }
      case DataIntegrityViolationException dataIntegrityViolationException -> {
        String errorMsg = "Conflict.";
        if(dataIntegrityViolationException.getCause() instanceof org.hibernate.exception.ConstraintViolationException hibernateConstraintViolationException) {
          errorMsg += " " + hibernateConstraintViolationException.getSQLException().getMessage();
        }
        return Pair.of(WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT.name(),
          errorMsg) ;
      }
      case BaseBusinessException businessException -> {
        return Pair.of(businessException.getCode(), businessException.getMessage());
      }
      default -> {
        return Pair.of(null, ex.getMessage());
      }
    }
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }
}
