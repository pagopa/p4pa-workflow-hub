package it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaListRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaRetrieverActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.payments-reporting-pago-pa")
public class PaymentsReportingPagoPaWfConfig extends BaseWfConfig {

  public OrganizationPaymentsReportingPagoPaListRetrieverActivity buildOrganizationPaymentsReportingPagoPaListRetrieverActivityStub() {
    return Workflow.newActivityStub(OrganizationPaymentsReportingPagoPaListRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public OrganizationPaymentsReportingPagoPaRetrieverActivity buildOrganizationPaymentsReportingPagoPaRetrieverActivityStub() {
    return Workflow.newActivityStub(OrganizationPaymentsReportingPagoPaRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
