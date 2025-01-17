package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.treasury-opi-ingestion")
public class TreasuryOpiIngestionWfConfig extends BaseWfConfig {

  public TreasuryOpiIngestionActivity buildTreasuryOpiIngestionActivityStub() {
    return Workflow.newActivityStub(TreasuryOpiIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UpdateIngestionFlowStatusActivity buildUpdateIngestionFlowStatusActivityStub() {
    return Workflow.newActivityStub(UpdateIngestionFlowStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SendEmailIngestionFlowActivity buildSendEmailIngestionFlowActivityStub() {
    return Workflow.newActivityStub(SendEmailIngestionFlowActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public NotifyTreasuryToIufClassificationActivity buildNotifyTreasuryToIufClassificationActivityStub() {
    return Workflow.newActivityStub(NotifyTreasuryToIufClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
