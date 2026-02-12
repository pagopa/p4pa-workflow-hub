package it.gov.pagopa.pu.workflow.wf.pagopa.send.service;

import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV25DTO;

public interface SendEventStreamProcessingService {
  String processSendStreamEvent(String sendStreamId, ProgressResponseElementV25DTO streamEvent);
}
