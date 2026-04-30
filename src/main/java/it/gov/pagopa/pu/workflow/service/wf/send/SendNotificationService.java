package it.gov.pagopa.pu.workflow.service.wf.send;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface SendNotificationService {
  WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId);
  WorkflowCreatedDTO sendNotificationStreamConsume(String sendStreamId);
}
