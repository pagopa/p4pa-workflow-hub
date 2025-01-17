package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface StartIufClassificationActivity {

  void signalIufClassificationWithStart(Long organizationId, String iuf, String treasuryId);

}
