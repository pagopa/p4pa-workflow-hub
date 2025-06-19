package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface NotifyTreasuryToIufClassificationActivity {

  @ActivityMethod
  void signalTreasuryIufClassificationWithStart(Long organizationId, String iuf, String treasuryId);

}
