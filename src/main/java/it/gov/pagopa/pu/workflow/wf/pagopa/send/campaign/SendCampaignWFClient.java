package it.gov.pagopa.pu.workflow.wf.pagopa.send.campaign;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;

import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf.SendNotificationStreamConsumeWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendCampaignWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public SendCampaignWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startSendCampaignCountersRefresh() {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_NOTIFICATION; //TODO create/refer new task queue
    String uuid = UUID.randomUUID().toString(); //TODO understand if there is an input to this WF, or if it's ok to use a different id for each invocation
    String workflowId = generateWorkflowId(uuid, SendNotificationStreamConsumeWF.class); //TODO change WF reference to newly created WF

    SendNotificationStreamConsumeWF workflow = workflowService.buildWorkflowStubToStartNew( //TODO change WF reference to newly created WF
      SendNotificationStreamConsumeWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::readSendStream, uuid); //TODO change method reference to method of newly created WF
  }
}
