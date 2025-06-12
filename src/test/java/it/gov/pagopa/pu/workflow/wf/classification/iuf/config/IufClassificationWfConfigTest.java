package it.gov.pagopa.pu.workflow.wf.classification.iuf.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class IufClassificationWfConfigTest {

  private final IufClassificationWfConfig config = new IufClassificationWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    StartTransferClassificationActivity.class, StartTransferClassificationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
