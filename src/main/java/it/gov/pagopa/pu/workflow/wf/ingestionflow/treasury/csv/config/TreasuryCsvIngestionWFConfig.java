package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csv.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.csv.TreasuryCsvIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.config.TreasuryIngestionWfConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.treasury-csv-ingestion")
public class TreasuryCsvIngestionWFConfig extends TreasuryIngestionWfConfig {
  public TreasuryCsvIngestionActivity buildTreasuryCsvIngestionActivityStub() {
    return Workflow.newActivityStub(TreasuryCsvIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
