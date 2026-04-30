package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.LocalDate;

@ActivityInterface
public interface ScheduleCheckDpExpirationActivity {

  @ActivityMethod
  void scheduleNextCheckDpExpiration(Long debtPositionId, LocalDate nextDueDate);

}
