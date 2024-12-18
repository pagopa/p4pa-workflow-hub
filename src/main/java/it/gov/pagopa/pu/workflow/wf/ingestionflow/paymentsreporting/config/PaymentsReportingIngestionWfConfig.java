package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.utility.SendEmailIngestionFlowActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.payments-reporting-ingestion")
public class PaymentsReportingIngestionWfConfig extends BaseWfConfig {

  public PaymentsReportingIngestionFlowFileActivity buildGetPaymentsReportingIngestionFlowFileActivityStub() {
    return Workflow.newActivityStub(PaymentsReportingIngestionFlowFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UpdateIngestionFlowStatusActivity buildGetUpdateIngestionFlowStatusActivityStub() {
    return Workflow.newActivityStub(UpdateIngestionFlowStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SendEmailIngestionFlowActivity buildGetSendEmailIngestionFlowActivityStub() {
    return Workflow.newActivityStub(SendEmailIngestionFlowActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}

