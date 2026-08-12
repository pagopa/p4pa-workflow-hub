package it.gov.pagopa.pu.workflow.exception.common;

import it.gov.pagopa.payhub.activities.exception.common.*;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.workflow.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoderService;
import jakarta.persistence.RollbackException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class CommonExceptionHandler {

  private static final ExceptionMessageTranscoderService exceptionMessageTranscoderService = new ExceptionMessageTranscoderService();

//region Spring Data Rest
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<WorkflowErrorDTO> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, WorkflowErrorDTO.CategoryEnum.WORKFLOW_NOT_FOUND);
  }
//endregion

//region Spring Data
  @ExceptionHandler({TransactionException.class})
  public ResponseEntity<WorkflowErrorDTO> handleTransactionException(TransactionException ex, HttpServletRequest request) {
    if (ex.getCause() instanceof RollbackException rollbackException && rollbackException.getCause() instanceof ValidationException validationException) {
      return handleViolationException(validationException, request);
    } else {
      return handleRuntimeException(ex, request);
    }
  }

  @ExceptionHandler({DataIntegrityViolationException.class})
  public ResponseEntity<WorkflowErrorDTO> handleDataIntegrityViolationException(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT);
  }

  @ExceptionHandler({CannotAcquireLockException.class})
  public ResponseEntity<WorkflowErrorDTO> handleCannotAcquireLockException(CannotAcquireLockException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.TOO_MANY_REQUESTS, WorkflowErrorDTO.CategoryEnum.WORKFLOW_TOO_MANY_REQUESTS);
  }
//endregion

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<WorkflowErrorDTO> handleConflictException(ConflictException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.CONFLICT, WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<WorkflowErrorDTO> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.FORBIDDEN, WorkflowErrorDTO.CategoryEnum.WORKFLOW_FORBIDDEN);
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class, InvalidValueException.class})
  public ResponseEntity<WorkflowErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST);
  }

  @ExceptionHandler(NotAuthorizedException.class)
  public ResponseEntity<WorkflowErrorDTO> handleNotAuthorizedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.UNAUTHORIZED, WorkflowErrorDTO.CategoryEnum.WORKFLOW_UNAUTHORIZED);
  }

  @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
  public ResponseEntity<WorkflowErrorDTO> handleInvokedHttpClientTooManyRequestsError(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.TOO_MANY_REQUESTS, WorkflowErrorDTO.CategoryEnum.WORKFLOW_TOO_MANY_REQUESTS);
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

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<WorkflowErrorDTO> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.NOT_FOUND, WorkflowErrorDTO.CategoryEnum.WORKFLOW_NOT_FOUND);
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<WorkflowErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, WorkflowErrorDTO.CategoryEnum.WORKFLOW_GENERIC_ERROR);
  }

  @ExceptionHandler({AuthorizationDeniedException.class})
  public ResponseEntity<WorkflowErrorDTO> handleAuthorizationDeniedException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.FORBIDDEN, WorkflowErrorDTO.CategoryEnum.WORKFLOW_FORBIDDEN);
  }

  public static ResponseEntity<WorkflowErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatus httpStatus, WorkflowErrorDTO.CategoryEnum errorEnum) {
    logException(ex, request, httpStatus);

    ExceptionMessageTranscoded code2message = Optional.of(request.getRequestURI())
      .filter(path -> path.contains("/crud/"))
      .map(path -> buildCrudErrorMessage(path, httpStatus, ex))
      .orElseGet(() -> buildReturnedMessage(ex));

    String code = Objects.requireNonNullElse(code2message.getCode(), errorEnum.getValue());
    String message = code2message.getMessage();
    List<ErrorFieldDTO> fields = code2message.getFields();

    return ResponseEntity
      .status(httpStatus)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new WorkflowErrorDTO(errorEnum, code, message, fields, Utilities.getTraceId()));
  }

  public static void logException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus) {
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

  private static ExceptionMessageTranscoded buildCrudErrorMessage(String requestPath, HttpStatus httpStatus, Exception ex) {
    if(ex instanceof BaseBusinessException) {
      return buildReturnedMessage(ex);
    } else if (ex.getCause() instanceof BaseBusinessException causeBusinessException) {
      return buildReturnedMessage(causeBusinessException);
    }
    String entity = requestPath.split("/crud/")[1].split("/")[0].replaceAll("s$", "");
    String entityCode = entity.replace("-", "_").toUpperCase();
    ExceptionMessageTranscoded error = buildReturnedMessage(ex);
    return new ExceptionMessageTranscoded(entityCode + "_" + httpStatus.name(), error.getMessage(), error.getFields());
  }

  private static ExceptionMessageTranscoded buildReturnedMessage(Exception ex) {
    return exceptionMessageTranscoderService.transcode(ex);
  }

  public static String getRequestDetails(HttpServletRequest request) {
    String method = Objects.requireNonNullElse(request.getMethod(), "")
      .replace('\n', '_')
      .replace('\r', '_');
    String requestUri = Objects.requireNonNullElse(request.getRequestURI(), "")
      .replace('\n', '_')
      .replace('\r', '_');
    return "%s %s".formatted(method, requestUri);
  }
}
