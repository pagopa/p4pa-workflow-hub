package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class IoNotificationWfConfigTest {

  private final IoNotificationWfConfig config = new IoNotificationWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    PublishPaymentEventActivity.class, PublishPaymentEventActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
