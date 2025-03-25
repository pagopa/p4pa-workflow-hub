package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;

/** It will publish an event related to DebtPosition on Kafka */
@ActivityInterface
public interface PublishPaymentEventActivity {
  @ActivityMethod
  void publishDebtPositionEvent(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest);
  @ActivityMethod
  void publishDebtPositionErrorEvent(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest);
}
