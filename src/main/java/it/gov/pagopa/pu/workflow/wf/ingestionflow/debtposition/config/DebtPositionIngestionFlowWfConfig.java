package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.MassiveNoticeGenerationStatusRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-ingestion")
public class DebtPositionIngestionFlowWfConfig extends BaseWfConfig {

  public IngestionFlowFileProcessingLockerActivity buildIngestionFlowFileProcessingLockerActivityStub() {
    return Workflow.newActivityStub(IngestionFlowFileProcessingLockerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public InstallmentIngestionFlowFileActivity buildInstallmentIngestionFlowFileActivityStub() {
    return Workflow.newActivityStub(InstallmentIngestionFlowFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UpdateIngestionFlowStatusActivity buildUpdateIngestionFlowStatusActivityStub() {
    return Workflow.newActivityStub(UpdateIngestionFlowStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SendEmailIngestionFlowActivity buildSendEmailIngestionFlowActivityStub() {
    return Workflow.newActivityStub(SendEmailIngestionFlowActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SynchronizeIngestedDebtPositionActivity buildSynchronizeIngestedDebtPositionActivityStub() {
    return Workflow.newActivityStub(SynchronizeIngestedDebtPositionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public MassiveNoticeGenerationStatusRetrieverActivity buildMassiveNoticeGenerationStatusRetrieverActivity() {
    return Workflow.newActivityStub(MassiveNoticeGenerationStatusRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
