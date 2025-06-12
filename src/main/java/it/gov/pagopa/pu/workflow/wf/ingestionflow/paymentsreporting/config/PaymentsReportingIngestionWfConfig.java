package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.payments-reporting-ingestion")
public class PaymentsReportingIngestionWfConfig extends BaseWfConfig {

  public PaymentsReportingIngestionFlowFileActivity buildPaymentsReportingIngestionFlowFileActivityStub() {
    return Workflow.newActivityStub(PaymentsReportingIngestionFlowFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public NotifyPaymentsReportingToIufClassificationActivity buildNotifyPaymentsReportingToIufClassificationActivityStub() {
    return Workflow.newActivityStub(NotifyPaymentsReportingToIufClassificationActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL,
        this));
  }

}

