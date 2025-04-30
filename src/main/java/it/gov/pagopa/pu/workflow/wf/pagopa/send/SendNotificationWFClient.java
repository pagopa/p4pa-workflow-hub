package it.gov.pagopa.pu.workflow.wf.pagopa.send;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.mapper.WorkflowCreatedMapper;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfretrievedt.SendNotificationDateRetrieveWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfretrievedt.SendNotificationDateRetrieveWFImpl;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationProcessWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendNotificationWFClient {

  private final WorkflowService workflowService;

  public SendNotificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowCreatedDTO startSendNotificationProcess(String sendNotificationId) {
    log.debug("Starting send notification process having id {}", sendNotificationId);
    String taskQueue = SendNotificationProcessWFImpl.TASK_QUEUE_SEND_NOTIFICATION_PROCESS;
    String workflowId = generateWorkflowId(sendNotificationId, SendNotificationProcessWF.class);

    SendNotificationProcessWF workflow = workflowService.buildWorkflowStub(
      SendNotificationProcessWF.class,
      taskQueue,
      workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(WorkflowClient.start(workflow::sendNotificationProcess, sendNotificationId));
    logWfExec(wfExec);
    return wfExec;
  }

  public WorkflowCreatedDTO startSendNotificationDateRetrieve(String sendNotificationId) {
    String taskQueue = SendNotificationDateRetrieveWFImpl.TASK_QUEUE_SEND_NOTIFICATION_DATE_RETRIEVE;
    String workflowId = generateWorkflowId(sendNotificationId, SendNotificationDateRetrieveWF.class);

    SendNotificationDateRetrieveWF workflow = workflowService.buildWorkflowStub(
      SendNotificationDateRetrieveWF.class,
      taskQueue,
      workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(WorkflowClient.start(workflow::sendNotificationDateRetrieve, sendNotificationId));
    logWfExec(wfExec);
    return wfExec;
  }

  public void scheduleSendNotificationDateRetrieve(String sendNotificationId, Duration nextSchedule) {
    log.debug("Starting scheduleSendNotificationDateRetrieve having id {}", sendNotificationId);
    String taskQueue = SendNotificationDateRetrieveWFImpl.TASK_QUEUE_SEND_NOTIFICATION_DATE_RETRIEVE;
    String workflowId = generateWorkflowId(sendNotificationId, SendNotificationDateRetrieveWF.class);

    SendNotificationDateRetrieveWF workflow = workflowService.buildWorkflowStubDelayed(
      SendNotificationDateRetrieveWF.class,
      taskQueue,
      workflowId,
      nextSchedule);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(WorkflowClient.start(workflow::sendNotificationDateRetrieve, sendNotificationId));
    logWfExec(wfExec);
  }

  private static void logWfExec(WorkflowCreatedDTO wfExec) {
    log.info("Started workflow: {}", wfExec);
  }
}
