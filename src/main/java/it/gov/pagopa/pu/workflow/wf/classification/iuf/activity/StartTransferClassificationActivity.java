package it.gov.pagopa.pu.workflow.wf.classification.iuf.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface StartTransferClassificationActivity {

  @ActivityMethod
  void signalTransferClassificationWithStart(Long organizationId, String iuv, String iur, int transferIndex);

}
