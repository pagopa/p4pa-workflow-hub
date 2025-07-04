package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessments.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.assessments.AssessmentsIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.assessments-ingestion")
public class AssessmentsIngestionWFConfig extends BaseWfConfig {

  public AssessmentsIngestionActivity buildAssessmentsIngestionActivityStub() {
    return Workflow.newActivityStub(AssessmentsIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}

