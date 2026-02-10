package it.gov.pagopa.pu.workflow.service.wf.send;

import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV25DTO;
import it.gov.pagopa.pu.workflow.dto.SendEventStreamProcessResult;

public interface SendEventStreamProcessingService {
  SendEventStreamProcessResult processSendStreamEvent(String sendStreamId, ProgressResponseElementV25DTO streamEvent);
}
