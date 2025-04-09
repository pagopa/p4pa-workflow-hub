package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = SynchronizeDebtPositionWfConfig.TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY)
public class CancelReductionExpirationScheduleActivityImpl implements CancelReductionExpirationScheduleActivity {

  private final WorkflowService workflowService;

  public CancelReductionExpirationScheduleActivityImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public void cancelScheduling(String workflowId) {
    workflowService.cancelWorkflow(workflowId);
  }
}
