package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface StartMassiveNoticesGenerationWFActivity {
  @ActivityMethod
  void startMassiveNoticesGenerationWF(Long ingestionFlowFileId);
}
