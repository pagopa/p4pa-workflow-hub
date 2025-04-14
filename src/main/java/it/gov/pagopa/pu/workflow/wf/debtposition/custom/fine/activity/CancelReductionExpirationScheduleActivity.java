package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface CancelReductionExpirationScheduleActivity {

  @ActivityMethod
  void cancelReductionPeriodExpirationScheduling(Long debtPositionId);
}
