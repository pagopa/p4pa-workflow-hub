package it.gov.pagopa.pu.workflow.exception.transcoder.handler;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public class DataIntegrityViolationExceptionMessageTranscoder implements ExceptionMessageTranscoder<DataIntegrityViolationException> {

  private static final String UNIQUE_KEY_VIOLATION_CODE = "23505";

  @Override
  public ExceptionMessageTranscoded transcode(DataIntegrityViolationException dataIntegrityViolationException) {
    String errorMsg = "Data integrity violation.";
    boolean isUniqueViolation = false;

    if (dataIntegrityViolationException.getCause() instanceof ConstraintViolationException hibernateConstraintViolationException) {
      java.sql.SQLException sqlEx = hibernateConstraintViolationException.getSQLException();
      errorMsg += " "+sqlEx.getMessage();
      String sqlState = sqlEx.getSQLState();

      if (UNIQUE_KEY_VIOLATION_CODE.equals(sqlState)) {
        isUniqueViolation = true;
        errorMsg = "Conflict. "+sqlEx.getMessage();
      }
    }

    String categoryCode = isUniqueViolation
      ? WorkflowErrorDTO.CategoryEnum.WORKFLOW_CONFLICT.getValue()
      : WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST.getValue();

    return new ExceptionMessageTranscoded(
      categoryCode,
      errorMsg,
      null);
  }
}
