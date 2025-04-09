package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = SynchronizeDebtPositionWfConfig.TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY)
public class ScheduleReductionExpirationActivityImpl implements ScheduleReductionExpirationActivity {

  private final DebtPositionFineClient debtPositionFineClient;

  public ScheduleReductionExpirationActivityImpl(DebtPositionFineClient debtPositionFineClient) {
    this.debtPositionFineClient = debtPositionFineClient;
  }

  @Override
  public String expireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig) {
    return debtPositionFineClient.expireFineReduction(debtPositionId, wfExecutionConfig);
  }
}
