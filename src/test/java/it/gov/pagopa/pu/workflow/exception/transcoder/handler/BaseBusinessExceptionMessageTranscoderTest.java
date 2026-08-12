package it.gov.pagopa.pu.workflow.exception.transcoder.handler;

import it.gov.pagopa.payhub.activities.config.rest.PuErrorDTO;
import it.gov.pagopa.payhub.activities.exception.common.BaseBusinessException;
import it.gov.pagopa.payhub.activities.exception.common.InvalidValueException;
import it.gov.pagopa.pu.workflow.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.workflow.exception.transcoder.ExceptionMessageTranscoded;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class BaseBusinessExceptionMessageTranscoderTest {

  private final BaseBusinessExceptionMessageTranscoder transcoder = new BaseBusinessExceptionMessageTranscoder();

  @Test
  void testTranscode() {
    // Given
    BaseBusinessException businessException = new InvalidValueException("code", "message",
      List.of(new PuErrorDTO.ErrorFieldDTO("ERRORFIELD", "ERRORCODE", "ERRORMESSAGE")
      ));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(businessException);

    // Then
    List<ErrorFieldDTO> fields = result.getFields();
    for (int i = 0; i < result.getFields().size(); i++) {
      PuErrorDTO.ErrorFieldDTO puErrorFieldDTO = businessException.getFields().get(i);
      ErrorFieldDTO workflowErrorFieldDTO = result.getFields().get(i);

      Assertions.assertEquals(puErrorFieldDTO.getField(), workflowErrorFieldDTO.getField());
      Assertions.assertEquals(puErrorFieldDTO.getError(), workflowErrorFieldDTO.getError());
      Assertions.assertEquals(puErrorFieldDTO.getMessage(), workflowErrorFieldDTO.getMessage());
    }

    Assertions.assertEquals(
      new ExceptionMessageTranscoded(businessException.getCode(), businessException.getMessage(), fields),
      result);
  }
}
