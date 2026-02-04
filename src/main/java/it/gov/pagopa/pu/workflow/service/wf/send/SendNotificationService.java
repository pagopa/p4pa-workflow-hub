package it.gov.pagopa.pu.workflow.service.wf.send;


import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface SendNotificationService {
  WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId);
  WorkflowCreatedDTO sendNotificationDateRetrieve(String sendNotificationId);
  WorkflowCreatedDTO sendNotificationStreamConsume(Long organizationId, String sendStreamId);
}
