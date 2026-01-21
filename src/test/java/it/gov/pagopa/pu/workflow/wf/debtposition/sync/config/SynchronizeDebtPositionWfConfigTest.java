package it.gov.pagopa.pu.workflow.wf.debtposition.sync.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class SynchronizeDebtPositionWfConfigTest {

  private final SynchronizeDebtPositionWfConfig config = new SynchronizeDebtPositionWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    PublishPaymentEventActivity.class, PublishPaymentEventActivityImpl.class,
    CancelCheckDpExpirationScheduleActivity.class, CancelCheckDpExpirationScheduleActivityImpl.class,
    ScheduleCheckDpExpirationActivity.class, ScheduleCheckDpExpirationActivityImpl.class,
    InvokeIONotificationActivity.class, InvokeIONotificationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
