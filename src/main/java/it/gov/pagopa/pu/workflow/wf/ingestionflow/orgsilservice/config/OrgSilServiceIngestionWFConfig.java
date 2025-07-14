package it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.orgsilservice.OrgSilServiceIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.orgsilservice-ingestion")
public class OrgSilServiceIngestionWFConfig extends BaseWfConfig {

  public OrgSilServiceIngestionActivity buildOrgSilServiceIngestionActivityStub() {
    return Workflow.newActivityStub(OrgSilServiceIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}
