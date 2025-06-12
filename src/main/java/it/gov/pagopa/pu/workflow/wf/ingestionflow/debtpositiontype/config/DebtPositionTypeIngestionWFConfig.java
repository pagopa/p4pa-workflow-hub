package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtpositiontype.DebtPositionTypeIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-type-ingestion")
public class DebtPositionTypeIngestionWFConfig extends BaseWfConfig {

  public DebtPositionTypeIngestionActivity buildDebtPositionTypeIngestionActivityStub() {
    return Workflow.newActivityStub(DebtPositionTypeIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}

