package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.activity.ActivityInterface;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;

@ActivityInterface
public interface PublishPaymentEventActivity {
  void publish(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, String paymentEventDescription);
}
