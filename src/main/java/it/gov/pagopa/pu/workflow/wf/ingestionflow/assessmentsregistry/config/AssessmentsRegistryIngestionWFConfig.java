package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.assessmentsregistry.AssessmentsRegistryIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.assessments-registry-ingestion")
public class AssessmentsRegistryIngestionWFConfig extends BaseWfConfig {

  public AssessmentsRegistryIngestionActivity buildAssessmentsRegistryIngestionActivityStub() {
    return Workflow.newActivityStub(AssessmentsRegistryIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}

