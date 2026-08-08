package it.gov.pagopa.pu.workflow.exception.transcoder.handler;


import it.gov.pagopa.payhub.activities.config.rest.PuErrorDTO;
import it.gov.pagopa.payhub.activities.exception.common.BaseBusinessException;
import it.gov.pagopa.pu.workflow.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoder;

import java.util.List;

public class BaseBusinessExceptionMessageTranscoder implements ExceptionMessageTranscoder<BaseBusinessException> {
  @Override
  public ExceptionMessageTranscoded transcode(BaseBusinessException businessException) {
    return new ExceptionMessageTranscoded(businessException.getCode(), businessException.getMessage(), transcodeErrorFieldDTO(businessException.getFields()));
  }

  private List<ErrorFieldDTO> transcodeErrorFieldDTO(List<PuErrorDTO.ErrorFieldDTO> fields) {
    return fields == null
      ? null
      : fields.stream()
              .map(field -> new ErrorFieldDTO(field.getField(), field.getError(), field.getMessage()))
              .toList();
  }
}
