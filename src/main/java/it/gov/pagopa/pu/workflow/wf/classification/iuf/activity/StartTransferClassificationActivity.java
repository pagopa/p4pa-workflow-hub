package it.gov.pagopa.pu.workflow.wf.classification.iuf.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface StartTransferClassificationActivity {

  void signalTransferClassificationWithStart(Long organizationId, String iuv, String iur, int transferIndex);

}
