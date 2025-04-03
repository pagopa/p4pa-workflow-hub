package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

@ActivityInterface
public interface InvokeSyncDebtPositionActivity {

  @ActivityMethod
  String synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, boolean massive, GenericWfExecutionConfig wfExecutionConfig);
}
