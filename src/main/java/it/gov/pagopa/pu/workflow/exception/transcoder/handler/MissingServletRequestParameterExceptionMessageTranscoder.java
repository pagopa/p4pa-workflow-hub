package it.gov.pagopa.pu.workflow.exception.transcoder.handler;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

public class MissingServletRequestParameterExceptionMessageTranscoder implements ExceptionMessageTranscoder<MissingServletRequestParameterException> {

  @Override
  public ExceptionMessageTranscoded transcode(MissingServletRequestParameterException missingServletRequestParameterException) {
    return new ExceptionMessageTranscoded(
      WorkflowErrorDTO.CategoryEnum.WORKFLOW_BAD_REQUEST.getValue(),
      missingServletRequestParameterException.getMessage(),
      List.of(new ErrorFieldDTO(missingServletRequestParameterException.getParameterName(), "NotNull", missingServletRequestParameterException.getMessage())));
  }
}
