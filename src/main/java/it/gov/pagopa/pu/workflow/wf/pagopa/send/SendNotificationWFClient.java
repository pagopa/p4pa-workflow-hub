package it.gov.pagopa.pu.workflow.wf.pagopa.send;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfretrievedt.SendNotificationDateRetrieveWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationStreamConsumeWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendNotificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public SendNotificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startSendNotificationProcess(String sendNotificationId) {
    log.debug("Starting send notification process having id {}", sendNotificationId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_NOTIFICATION;
    String workflowId = generateWorkflowId(sendNotificationId, SendNotificationProcessWF.class);

    SendNotificationProcessWF workflow = workflowService.buildWorkflowStubToStartNew(
      SendNotificationProcessWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::sendNotificationProcess, sendNotificationId);
  }

  public WorkflowCreatedDTO startSendNotificationDateRetrieve(String sendNotificationId) {
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_LOW_PRIORITY;
    String workflowId = generateWorkflowId(sendNotificationId, SendNotificationDateRetrieveWF.class);

    SendNotificationDateRetrieveWF workflow = workflowService.buildWorkflowStubToStartNew(
      SendNotificationDateRetrieveWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::sendNotificationDateRetrieve, sendNotificationId);
  }

  public void scheduleSendNotificationDateRetrieve(String sendNotificationId, Duration nextSchedule) {
    log.debug("Starting scheduleSendNotificationDateRetrieve having id {}", sendNotificationId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_LOW_PRIORITY;
    String workflowId = generateWorkflowId(sendNotificationId, SendNotificationDateRetrieveWF.class);

    SendNotificationDateRetrieveWF workflow = workflowService.buildWorkflowStubDelayed(
      SendNotificationDateRetrieveWF.class,
      taskQueue,
      workflowId,
      nextSchedule);
    workflowClientService.start(workflow::sendNotificationDateRetrieve, sendNotificationId);
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
