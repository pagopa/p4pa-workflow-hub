package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.OffsetDateTime;

@ActivityInterface
public interface ScheduleCheckDpExpirationActivity {

  @ActivityMethod
  void scheduleNextCheckDpExpiration(Long debtPositionId, OffsetDateTime dateTime);

}
