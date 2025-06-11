package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC_LOCAL)
public class ScheduleReductionExpirationActivityImpl implements ScheduleReductionExpirationActivity {

  private final DebtPositionFineClient debtPositionFineClient;

  public ScheduleReductionExpirationActivityImpl(DebtPositionFineClient debtPositionFineClient) {
    this.debtPositionFineClient = debtPositionFineClient;
  }

  @Override
  public WorkflowCreatedDTO scheduleExpireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig, OffsetDateTime fineReductionExpirationDateTime) {
    return debtPositionFineClient.scheduleExpireFineReduction(debtPositionId, wfExecutionConfig, fineReductionExpirationDateTime);
  }
}
