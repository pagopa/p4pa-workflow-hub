package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.poste.TreasuryPosteIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.config.TreasuryIngestionWfConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.treasury-poste-ingestion")
public class TreasuryPosteIngestionWFConfig extends TreasuryIngestionWfConfig {

  public TreasuryPosteIngestionActivity buildTreasuryPosteIngestionActivityStub() {
    return Workflow.newActivityStub(TreasuryPosteIngestionActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}
