package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/** It will cancel the expiration task schedule if exists */
@ActivityInterface
public interface CancelCheckDpExpirationScheduleActivity {
  @ActivityMethod
  void cancel(Long debtPositionId);
}
