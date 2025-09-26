package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.xls.TreasuryXlsIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.config.TreasuryIngestionWfConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.treasury-xls-ingestion")
public class TreasuryXlsIngestionWFConfig extends TreasuryIngestionWfConfig {
  public TreasuryXlsIngestionActivity buildTreasuryXlsIngestionActivityStub() {
    return Workflow.newActivityStub(TreasuryXlsIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
