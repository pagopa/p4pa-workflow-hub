package it.gov.pagopa.pu.workflow.wf.classification.assessments.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.assessments.AssessmentsClassificationActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.NotifyAssessmentClassificationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.assessments-classification")
public class ClassifyAssessmentsWfConfig extends BaseWfConfig {

  public AssessmentsClassificationActivity buildAssessmentsClassificationActivityStub() {
    return Workflow.newActivityStub(AssessmentsClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public NotifyAssessmentClassificationActivity buildNotifyAssessmentClassificationActivityStub() {
    return Workflow.newActivityStub(NotifyAssessmentClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL,
      this));
  }
}
