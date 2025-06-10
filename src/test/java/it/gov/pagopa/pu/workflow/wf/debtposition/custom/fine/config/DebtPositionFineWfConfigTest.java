package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivityImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.CancelReductionExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.CancelReductionExpirationScheduleActivityImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.ScheduleReductionExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.ScheduleReductionExpirationActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class DebtPositionFineWfConfigTest {

  private final DebtPositionFineWfConfig config = new DebtPositionFineWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    InvokeSyncDebtPositionActivity.class, InvokeSyncDebtPositionActivityImpl.class,
    CancelReductionExpirationScheduleActivity.class, CancelReductionExpirationScheduleActivityImpl.class,
    ScheduleReductionExpirationActivity.class, ScheduleReductionExpirationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
