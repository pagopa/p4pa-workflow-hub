package it.gov.pagopa.pu.workflow.exception;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
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
  public ResponseEntity<WorkflowErrorDTO> handleException(Exception ex) {
    log.error("Internal Server Error: ", ex);

    WorkflowErrorDTO error = new WorkflowErrorDTO();
    error.setCode(WorkflowErrorDTO.CodeEnum.GENERIC_ERROR);
    error.setMessage(ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);


  }
}
