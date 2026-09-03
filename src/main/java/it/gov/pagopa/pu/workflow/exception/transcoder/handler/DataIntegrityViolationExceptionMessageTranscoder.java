package it.gov.pagopa.pu.workflow.exception.transcoder.handler;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public class DataIntegrityViolationExceptionMessageTranscoder implements ExceptionMessageTranscoder<DataIntegrityViolationException> {

  @Override
  public ExceptionMessageTranscoded transcode(DataIntegrityViolationException dataIntegrityViolationException) {
    String errorMsg = "Conflict.";
    if(dataIntegrityViolationException.getCause() instanceof ConstraintViolationException hibernateConstraintViolationException) {
      errorMsg += " " + hibernateConstraintViolationException.getSQLException().getMessage();
    }
    return new ExceptionMessageTranscoded(
      WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT.getValue(),
      errorMsg,
      null) ;
  }
}
