package it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.dto.DebtPositionSendNotificationDTO;

/** It will publish an event related to DebtPosition on Kafka */
@ActivityInterface
public interface PublishSendNotificationPaymentEventActivity {
  @ActivityMethod
  void publishSendNotificationEvent(DebtPositionSendNotificationDTO sendNotification, PaymentEventRequestDTO sendEventRequest);
  @ActivityMethod
  void publishSendNotificationErrorEvent(DebtPositionSendNotificationDTO sendNotification, PaymentEventRequestDTO sendEventRequest);
}
