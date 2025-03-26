package it.gov.pagopa.pu.workflow.wf.classification.assessments.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.AssessmentsCreationActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.create-assessments")
public class CreateAssessmentsWFConfig extends BaseWfConfig {

  public AssessmentsCreationActivity buildAssessmentsCreationActivityStub() {
    return Workflow.newActivityStub(AssessmentsCreationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
