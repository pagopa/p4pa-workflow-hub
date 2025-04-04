package it.gov.pagopa.pu.workflow.service.send;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.SendNotificationWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendNotificationServiceImpl implements SendNotificationService {

  private final SendNotificationWFClient sendNotificationWFClient;

  public SendNotificationServiceImpl(SendNotificationWFClient sendNotificationWFClient) {
    this.sendNotificationWFClient = sendNotificationWFClient;
  }

  @Override
  public WorkflowCreatedDTO sendNotificationProcess(String sendNotificationId) {
    log.debug("Starting send notification process with sendNotificationId: {}", sendNotificationId);
    String workflowId = sendNotificationWFClient.startSendNotificationProcess(sendNotificationId);

    return buildWorkflowCreatedDTO(workflowId);
  }

  @Override
  public WorkflowCreatedDTO sendNotificationDateRetrieve(String sendNotificationId) {
    String workflowId = sendNotificationWFClient.startSendNotificationDateRetrieve(sendNotificationId);

    return buildWorkflowCreatedDTO(workflowId);
  }

  private WorkflowCreatedDTO buildWorkflowCreatedDTO(String workflowId) {
    return WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();
  }
}
