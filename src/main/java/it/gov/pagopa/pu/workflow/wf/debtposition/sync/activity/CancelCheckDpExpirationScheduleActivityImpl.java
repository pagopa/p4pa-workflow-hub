package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC_LOCAL)
public class CancelCheckDpExpirationScheduleActivityImpl implements CancelCheckDpExpirationScheduleActivity {

  private final CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient;

  public CancelCheckDpExpirationScheduleActivityImpl(CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient) {
    this.checkDebtPositionExpirationWfClient = checkDebtPositionExpirationWfClient;
  }

  @Override
  public void cancelExpirationSchedule(Long debtPositionId) {
    checkDebtPositionExpirationWfClient.cancelScheduling(debtPositionId);
  }
}
