package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity.NotifyPaymentNotificationToIudClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.activity.NotifyPaymentNotificationToIudClassificationActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class PaymentNotificationIngestionWfConfigTest {

  private final PaymentNotificationIngestionWfConfig config = new PaymentNotificationIngestionWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    NotifyPaymentNotificationToIudClassificationActivity.class, NotifyPaymentNotificationToIudClassificationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
