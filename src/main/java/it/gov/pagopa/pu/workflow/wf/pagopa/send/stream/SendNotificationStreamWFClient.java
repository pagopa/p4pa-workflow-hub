package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf.SendNotificationStreamConsumeWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendNotificationStreamWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public SendNotificationStreamWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startSendNotificationStreamConsume(String sendStreamId) {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_STREAM;
    String workflowId = generateWorkflowId(sendStreamId, SendNotificationStreamConsumeWF.class);

    SendNotificationStreamConsumeWF workflow = workflowService.buildWorkflowStubToStartNew(
      SendNotificationStreamConsumeWF.class,
      taskQueue,
      workflowId
    );
    return workflowClientService.start(workflow::readSendStream, sendStreamId);
  }

}
