package it.gov.pagopa.pu.workflow.wf.classification.assessments.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface StartAssessmentClassificationActivity {

  @ActivityMethod
  void signalAssessmentClassificationWithStart(Long organizationId, String iuv, String iud);
}
