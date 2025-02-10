package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity;

import io.temporal.activity.ActivityInterface;

import java.time.OffsetDateTime;

@ActivityInterface
public interface ScheduleCheckDpExpirationActivity {

  void scheduleNextCheckDpExpiration(Long debtPositionId, OffsetDateTime dateTime);

}
