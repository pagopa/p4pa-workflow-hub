package it.gov.pagopa.pu.workflow.wf.pagopa.send;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendNotificationWFClient {

  private final WorkflowService workflowService;

  public SendNotificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String sendNotificationProcess(String sendNotificationId) {
    log.info("Starting send notification process having id {}", sendNotificationId);
    String taskQueue = SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_PROCESS;
    String workflowId = generateWorkflowId(sendNotificationId, taskQueue);

    SendNotificationProcessWF workflow = workflowService.buildWorkflowStub(
      SendNotificationProcessWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::sendNotificationProcess, sendNotificationId);
    return workflowId;
  }
}
