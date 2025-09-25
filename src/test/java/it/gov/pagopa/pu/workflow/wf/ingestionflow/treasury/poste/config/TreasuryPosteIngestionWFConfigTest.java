package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivityImpl;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TreasuryPosteIngestionWFConfigTest {

  private final TreasuryPosteIngestionWFConfig config = new TreasuryPosteIngestionWFConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    NotifyTreasuryToIufClassificationActivity.class,
    NotifyTreasuryToIufClassificationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment()
    throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config,
      localActivityInterface2Impl);
  }

}
