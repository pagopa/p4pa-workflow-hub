package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csvcomplete.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class TreasuryCsvCompleteIngestionWFConfigTest {

  private final TreasuryCsvCompleteIngestionWFConfig config = new TreasuryCsvCompleteIngestionWFConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    NotifyTreasuryToIufClassificationActivity.class, NotifyTreasuryToIufClassificationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
