package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClientImpl.generateExpireFineReductionWorkflowId;

@Service
@Slf4j
@ActivityImpl(taskQueues = SynchronizeDebtPositionWfConfig.TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY)
public class CancelReductionExpirationScheduleActivityImpl implements CancelReductionExpirationScheduleActivity {

  private final WorkflowService workflowService;

  public CancelReductionExpirationScheduleActivityImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public void cancelReductionPeriodExpirationScheduling(Long debtPositionId) {
    String workflowId = generateExpireFineReductionWorkflowId(debtPositionId);
    workflowService.cancelWorkflow(workflowId);
  }
}
