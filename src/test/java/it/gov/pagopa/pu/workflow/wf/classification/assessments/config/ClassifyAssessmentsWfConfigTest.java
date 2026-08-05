package it.gov.pagopa.pu.workflow.wf.classification.assessments.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.NotifyAssessmentClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.NotifyAssessmentClassificationActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class ClassifyAssessmentsWfConfigTest {

  private final ClassifyAssessmentsWfConfig config = new ClassifyAssessmentsWfConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    NotifyAssessmentClassificationActivity.class, NotifyAssessmentClassificationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
