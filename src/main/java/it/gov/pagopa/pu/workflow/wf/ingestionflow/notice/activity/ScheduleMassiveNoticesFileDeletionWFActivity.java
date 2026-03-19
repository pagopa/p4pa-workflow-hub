package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.Duration;

@ActivityInterface
public interface ScheduleMassiveNoticesFileDeletionWFActivity {
  @ActivityMethod
  void scheduleMassiveNoticesFileDeletionWF(Long ingestionFlowFileId, Duration retentionDuration);
}
