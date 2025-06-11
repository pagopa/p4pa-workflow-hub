package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class BrokersPaymentsReportingPagoPaFetchWfConfigTest {

  private final BrokersPaymentsReportingPagoPaFetchWfConfig config = new BrokersPaymentsReportingPagoPaFetchWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of();

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
