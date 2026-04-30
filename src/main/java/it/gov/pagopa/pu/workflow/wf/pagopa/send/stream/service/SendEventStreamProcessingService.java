package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.service;

import it.gov.pagopa.pu.sendnotification.dto.generated.ProgressResponseElementV28DTO;

public interface SendEventStreamProcessingService {
  String processSendStreamEvent(String sendStreamId, ProgressResponseElementV28DTO streamEvent);
}
