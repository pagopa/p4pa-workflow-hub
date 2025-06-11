package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class SynchronizeTaxonomyPagoPaFetchWfConfigTest {

  private final SynchronizeTaxonomyPagoPaFetchWfConfig config = new SynchronizeTaxonomyPagoPaFetchWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of();

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
