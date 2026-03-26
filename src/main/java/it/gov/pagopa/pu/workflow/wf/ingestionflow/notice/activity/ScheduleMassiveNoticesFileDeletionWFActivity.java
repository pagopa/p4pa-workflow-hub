package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ScheduleMassiveNoticesFileDeletionWFActivity {
  @ActivityMethod
  void scheduleMassiveNoticesFileDeletionWF(Long ingestionFlowFileId);
}
