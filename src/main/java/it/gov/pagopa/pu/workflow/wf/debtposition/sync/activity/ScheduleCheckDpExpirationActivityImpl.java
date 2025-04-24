package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@ActivityImpl(taskQueues = {
  SynchronizeDebtPositionWfConfig.TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY,
  "CheckDebtPositionExpirationWF_LOCAL" //FIXME DEPRECATED, to remove once not more WF are scheduled on this
})
public class ScheduleCheckDpExpirationActivityImpl implements ScheduleCheckDpExpirationActivity {

  private final CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient;

  public ScheduleCheckDpExpirationActivityImpl(CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient) {
    this.checkDebtPositionExpirationWfClient = checkDebtPositionExpirationWfClient;
  }

  @Override
  public void scheduleNextCheckDpExpiration(Long debtPositionId, LocalDate nextDueDate) {
    checkDebtPositionExpirationWfClient.scheduleNextCheckDpExpiration(debtPositionId, nextDueDate);
  }
}
