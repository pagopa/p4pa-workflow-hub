package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

@ActivityInterface
public interface PublishPaymentEventActivity {
  @ActivityMethod
  void publishErrorEvent(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, String paymentEventDescription);
}
