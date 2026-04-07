package it.gov.pagopa.pu.workflow.wf.debtposition.massive.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ScheduleToSyncMassiveIbanUpdateWFActivity {
  @ActivityMethod
  void scheduleToSyncMassiveIbanUpdateWF(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban);
}
