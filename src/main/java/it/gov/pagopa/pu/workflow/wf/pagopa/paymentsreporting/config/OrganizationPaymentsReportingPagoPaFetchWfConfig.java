package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaListRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaRetrieverActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.payments-reporting-pagopa-organization-fetch")
public class OrganizationPaymentsReportingPagoPaFetchWfConfig extends BaseWfConfig {

  public OrganizationPaymentsReportingPagoPaListRetrieverActivity buildOrganizationPaymentsReportingPagoPaListRetrieverActivityStub() {
    return Workflow.newActivityStub(OrganizationPaymentsReportingPagoPaListRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public OrganizationPaymentsReportingPagoPaRetrieverActivity buildOrganizationPaymentsReportingPagoPaRetrieverActivityStub() {
    return Workflow.newActivityStub(OrganizationPaymentsReportingPagoPaRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
