package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtpositiontypeorg.DebtPositionTypeOrgIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-type-org-ingestion")
public class DebtPositionTypeOrgIngestionWFConfig extends BaseWfConfig {

  public DebtPositionTypeOrgIngestionActivity buildDebtPositionTypeOrgIngestionActivityStub() {
    return Workflow.newActivityStub(DebtPositionTypeOrgIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

}

