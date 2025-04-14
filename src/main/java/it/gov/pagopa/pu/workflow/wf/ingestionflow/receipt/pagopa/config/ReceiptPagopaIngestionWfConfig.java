package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaIngestionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaNotifySilActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaSendEmailActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.receipt-pagopa-ingestion")
public class ReceiptPagopaIngestionWfConfig extends BaseWfConfig {

  public ReceiptPagopaIngestionActivity buildReceiptPagopaIngestionActivityStub() {
    return Workflow.newActivityStub(ReceiptPagopaIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public UpdateIngestionFlowStatusActivity buildUpdateIngestionFlowStatusActivityStub() {
    return Workflow.newActivityStub(UpdateIngestionFlowStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public ReceiptPagopaSendEmailActivity buildReceiptPagopaSendEmailActivityStub() {
    return Workflow.newActivityStub(ReceiptPagopaSendEmailActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public ReceiptPagopaNotifySilActivity buildReceiptPagopaNotifySilActivityStub() {
    return Workflow.newActivityStub(ReceiptPagopaNotifySilActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
