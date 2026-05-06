package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wf.DeleteSendNotificationFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class DeleteSendNotificationFileWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public DeleteSendNotificationFileWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startDeleteSendNotificationExpiredFiles(String sendNotificationId) {
    log.debug("Starting delete send notification expired files process having id {}", sendNotificationId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_NOTIFICATION;
    String workflowId = generateWorkflowId(sendNotificationId, DeleteSendNotificationFileWF.class);

    DeleteSendNotificationFileWF workflow = workflowService.buildWorkflowStubToStartNew(
      DeleteSendNotificationFileWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::deleteSendNotificationExpiredFiles, sendNotificationId);
  }

}
