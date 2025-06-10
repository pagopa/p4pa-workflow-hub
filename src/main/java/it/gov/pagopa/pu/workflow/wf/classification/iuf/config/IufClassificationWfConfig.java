package it.gov.pagopa.pu.workflow.wf.classification.iuf.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIufActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IufClassificationActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.PaymentsReportingImplicitReceiptHandlerActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "workflow.iuf-classification")
public class IufClassificationWfConfig extends BaseWfConfig {

  public ClearClassifyIufActivity buildClearClassifyIufActivityStub() {
    return Workflow.newActivityStub(ClearClassifyIufActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public IufClassificationActivity buildIufClassificationActivityStub() {
    return Workflow.newActivityStub(IufClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PaymentsReportingImplicitReceiptHandlerActivity buildPaymentsReportingImplicitReceiptHandlerActivityStub() {
    return Workflow.newActivityStub(PaymentsReportingImplicitReceiptHandlerActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public StartTransferClassificationActivity buildStartTransferClassificationActivityStub() {
    return Workflow.newActivityStub(StartTransferClassificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL,
      this));
  }

}

