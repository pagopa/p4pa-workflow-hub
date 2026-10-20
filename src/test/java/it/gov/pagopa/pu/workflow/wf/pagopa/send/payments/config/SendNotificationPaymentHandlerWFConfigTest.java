package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class SendNotificationPaymentHandlerWFConfigTest {

  private final SendNotificationPaymentHandlerWFConfig config = new SendNotificationPaymentHandlerWFConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(PublishSendTimelineEventActivity.class, PublishSendTimelineEventActivityImpl.class);

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
