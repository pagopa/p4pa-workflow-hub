package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClientImpl.generateExpireFineReductionWorkflowId;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC_LOCAL)
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
