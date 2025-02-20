package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredRetrieverActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.payments-reporting-pagopa-brokers-fetch")
public class BrokersPaymentsReportingPagoPaFetchWfConfig extends BaseWfConfig {

  public BrokersRetrieverActivity buildBrokersRetrieverActivityStub() {
    return Workflow.newActivityStub(BrokersRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public OrganizationBrokeredRetrieverActivity buildOrganizationBrokeredRetrieverActivityStub() {
    return Workflow.newActivityStub(OrganizationBrokeredRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
