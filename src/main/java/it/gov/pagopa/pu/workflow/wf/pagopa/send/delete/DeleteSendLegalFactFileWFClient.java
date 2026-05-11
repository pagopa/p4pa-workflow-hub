package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wfsendlegalfact.DeleteSendLegalFactFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class DeleteSendLegalFactFileWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public DeleteSendLegalFactFileWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startDeleteSendLegalFactExpiredFiles(String sendNotificationId) {
    log.debug("Starting delete send expired legal facts process having id {}", sendNotificationId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    String workflowId = generateWorkflowId(sendNotificationId, DeleteSendLegalFactFileWF.class);

    DeleteSendLegalFactFileWF workflow = workflowService.buildWorkflowStubToStartNew(
      DeleteSendLegalFactFileWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::deleteSendLegalFactExpiredFiles, sendNotificationId);
  }

}
