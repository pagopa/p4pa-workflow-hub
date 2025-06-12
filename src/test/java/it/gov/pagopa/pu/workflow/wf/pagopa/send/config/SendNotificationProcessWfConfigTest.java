package it.gov.pagopa.pu.workflow.wf.pagopa.send.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivityImpl;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.ScheduleSendNotificationDateRetrieveActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.ScheduleSendNotificationDateRetrieveActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class SendNotificationProcessWfConfigTest {

  private final SendNotificationProcessWfConfig config = new SendNotificationProcessWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    PublishSendNotificationPaymentEventActivity.class, PublishSendNotificationPaymentEventActivityImpl.class,
    ScheduleSendNotificationDateRetrieveActivity.class, ScheduleSendNotificationDateRetrieveActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
