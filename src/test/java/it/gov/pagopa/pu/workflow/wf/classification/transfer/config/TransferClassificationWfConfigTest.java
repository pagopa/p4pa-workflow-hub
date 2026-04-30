package it.gov.pagopa.pu.workflow.wf.classification.transfer.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.StartAssessmentClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.StartAssessmentClassificationActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class TransferClassificationWfConfigTest {

  private final TransferClassificationWfConfig config = new TransferClassificationWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    StartAssessmentClassificationActivity.class, StartAssessmentClassificationActivityImpl.class);

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
