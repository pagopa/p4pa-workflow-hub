package it.gov.pagopa.pu.workflow.wf.pagopa.send.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;

/** It will publish an event related to DebtPosition on Kafka */
@ActivityInterface
public interface PublishSendNotificationPaymentEventActivity {
  @ActivityMethod
  void publishSendNotificationEvent(SendNotificationDTO sendNotification, PaymentEventType paymentEventType);
  @ActivityMethod
  void publishSendNotificationErrorEvent(SendNotificationDTO sendNotification, PaymentEventType paymentEventType, String errorDescription);
}
