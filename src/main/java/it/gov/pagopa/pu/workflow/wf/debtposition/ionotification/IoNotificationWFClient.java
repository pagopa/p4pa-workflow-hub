package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification.IoNotificationWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class IoNotificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public IoNotificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public void sendIoNotification(DebtPositionDTO debtPositionDTO, Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages) {
    log.info("Starting check debt position expiration WF: {}", debtPositionDTO.getDebtPositionId());
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY;
    String workflowId = generateWorkflowId(debtPositionDTO.getDebtPositionId(), IoNotificationWF.class);

    IoNotificationWF workflow = workflowService.buildWorkflowStubToStartNew(
      IoNotificationWF.class,
      taskQueue,
      workflowId);
    workflowClientService.start(workflow::sendIoNotification, debtPositionDTO, iudSyncCompleteDTOMap, ioMessages);
  }

}
