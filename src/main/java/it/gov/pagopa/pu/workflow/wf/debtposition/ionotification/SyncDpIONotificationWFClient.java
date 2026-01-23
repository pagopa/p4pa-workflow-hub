package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification.SyncDpIONotificationWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@Slf4j
@Service
public class SyncDpIONotificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public SyncDpIONotificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public void sendIoNotification(DebtPositionDTO debtPositionDTO, Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages) {
    log.info("Starting IO notification WF: {}", debtPositionDTO.getDebtPositionId());
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY;
    String dateTime = LocalDateTime.now().format(ISO_LOCAL_DATE_TIME);
    String workflowId = generateWorkflowId(String.format("%s-%s", debtPositionDTO.getDebtPositionId(), dateTime), SyncDpIONotificationWF.class);

    SyncDpIONotificationWF workflow = workflowService.buildWorkflowStubToStartNew(
      SyncDpIONotificationWF.class,
      taskQueue,
      workflowId);
    workflowClientService.start(workflow::sendIoNotification, debtPositionDTO, iudSyncCompleteDTOMap, ioMessages);
  }

}
