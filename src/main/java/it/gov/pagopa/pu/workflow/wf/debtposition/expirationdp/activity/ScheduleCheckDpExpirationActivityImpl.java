package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
@ActivityImpl(taskQueues = CheckDebtPositionExpirationWFImpl.TASK_QUEUE_SCHEDULE_CHECK_DP_EXPIRATION_LOCAL_ACTIVITY)
public class ScheduleCheckDpExpirationActivityImpl implements ScheduleCheckDpExpirationActivity {

  private final CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient;

  public ScheduleCheckDpExpirationActivityImpl(CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient) {
    this.checkDebtPositionExpirationWfClient = checkDebtPositionExpirationWfClient;
  }

  @Override
  public void scheduleNextCheckDpExpiration(Long debtPositionId, OffsetDateTime dateTime) {
    checkDebtPositionExpirationWfClient.scheduleNextCheckDpExpiration(debtPositionId, dateTime);
  }
}
