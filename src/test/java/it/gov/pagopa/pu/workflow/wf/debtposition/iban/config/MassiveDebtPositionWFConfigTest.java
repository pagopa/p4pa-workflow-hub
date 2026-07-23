package it.gov.pagopa.pu.workflow.wf.debtposition.iban.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.activity.ScheduleToSyncMassiveIbanUpdateWFActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.activity.ScheduleToSyncMassiveIbanUpdateWFActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class MassiveDebtPositionWFConfigTest {
  private final MassiveDebtPositionWFConfig config = new MassiveDebtPositionWFConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(ScheduleToSyncMassiveIbanUpdateWFActivity.class, ScheduleToSyncMassiveIbanUpdateWFActivityImpl.class);

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    config.setHeartbeatTimeoutInSeconds(300);
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
