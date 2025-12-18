package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine.SynchronizeFineWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class DebtPositionFineClientImpl implements DebtPositionFineClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public DebtPositionFineClientImpl(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  @Override
  public WorkflowCreatedDTO expireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig) {
    log.info("Starting check debt position reduction expiration WF: {}", debtPositionId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC;
    String workflowId = generateExpireFineReductionWorkflowId(debtPositionId);

    FineReductionOptionExpirationWF workflow = workflowService.buildWorkflowStubToStartNew(
      FineReductionOptionExpirationWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::expireFineReduction, debtPositionId, wfExecutionConfig);
  }

  @Override
  public WorkflowCreatedDTO scheduleExpireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig, OffsetDateTime fineReductionExpirationDateTime) {
    log.info("Starting schedule to check debt position reduction expiration WF: {}", debtPositionId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC;
    String workflowId = generateExpireFineReductionWorkflowId(debtPositionId);

    FineReductionOptionExpirationWF workflow = workflowService.buildWorkflowStubScheduled(
      FineReductionOptionExpirationWF.class,
      taskQueue,
      workflowId,
      fineReductionExpirationDateTime);
    return workflowClientService.start(workflow::expireFineReduction, debtPositionId, wfExecutionConfig);
  }

  @Override
  public WorkflowCreatedDTO synchronizeFineDP(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, Boolean massive, FineWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronizing fine WF: {}", debtPositionDTO.getDebtPositionId());
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC;
    String workflowId = generateWorkflowId(debtPositionDTO.getDebtPositionId(), SynchronizeFineWF.class);

    SynchronizeFineWF workflow = workflowService.buildWorkflowStubToStartNew(
      SynchronizeFineWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::synchronizeFineDP, debtPositionDTO, paymentEventRequest, massive, wfExecutionConfig);
  }

  public static String generateExpireFineReductionWorkflowId(Long debtPositionId) {
    return generateWorkflowId(debtPositionId, FineReductionOptionExpirationWF.class);
  }
}
