package it.gov.pagopa.pu.workflow.exception;

import it.gov.pagopa.pu.workflow.dto.generated.CreatePaymentIngestionWF500Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class WorkflowHubExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<CreatePaymentIngestionWF500Response> handleException(Exception ex) {
    log.error("Internal Server Error", ex);

    CreatePaymentIngestionWF500Response response = new CreatePaymentIngestionWF500Response("Internal Server Error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

}
