package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.LocalDate;

@ActivityInterface
public interface ScheduleMassiveNoticesFileDeletionWFActivity {
  @ActivityMethod
  void scheduleFileDeletion(Long ingestionFlowFileId, LocalDate scheduleDate);
}
