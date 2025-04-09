package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface CancelReductionExpirationScheduleActivity {

  @ActivityMethod
  void cancelScheduling(String workflowId);
}
