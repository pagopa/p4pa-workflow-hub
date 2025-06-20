package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface NotifyPaymentNotificationToIudClassificationActivity {

  @ActivityMethod
  void signalPaymentNotificationIudClassificationWithStart(Long organizationId, String iud);

}
