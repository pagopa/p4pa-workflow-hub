package it.gov.pagopa.pu.workflow.wf.assessments.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.assessments.AssessmentsRegistryCreationActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.create-assessments-registry")
public class CreateAssessmentsRegistryWFConfig extends BaseWfConfig {

  public AssessmentsRegistryCreationActivity buildAssessmentsRegistryCreationActivityStub() {
    return Workflow.newActivityStub(AssessmentsRegistryCreationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
