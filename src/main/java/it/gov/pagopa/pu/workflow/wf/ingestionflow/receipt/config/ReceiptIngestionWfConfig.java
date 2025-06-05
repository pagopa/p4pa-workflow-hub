package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.receipt-ingestion")
public class ReceiptIngestionWfConfig extends BaseWfConfig {

  public ReceiptIngestionActivity buildReceiptIngestionActivityStub() {
    return Workflow.newActivityStub(ReceiptIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
