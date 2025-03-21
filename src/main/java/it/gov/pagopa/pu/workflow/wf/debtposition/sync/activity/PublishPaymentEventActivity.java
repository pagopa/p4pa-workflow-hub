package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/** It will publish an event related to DebtPosition on Kafka */
@ActivityInterface
public interface PublishPaymentEventActivity {
  @ActivityMethod
  void publishEvent(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType);
  @ActivityMethod
  void publishErrorEvent(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, String paymentEventDescription);
}
