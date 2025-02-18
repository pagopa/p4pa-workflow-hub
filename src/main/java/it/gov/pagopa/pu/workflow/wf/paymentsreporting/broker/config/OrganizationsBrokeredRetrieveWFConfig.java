package it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredRetrieverActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve.OrganizationsBrokeredRetrieveWFImpl;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.activity.ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.organizations-brokered")
public class OrganizationsBrokeredRetrieveWFConfig extends BaseWfConfig {

  public BrokersRetrieverActivity buildBrokersRetrieverActivityStub() {
    return Workflow.newActivityStub(BrokersRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public OrganizationBrokeredRetrieverActivity buildOrganizationBrokeredRetrieverActivityStub() {
    return Workflow.newActivityStub(OrganizationBrokeredRetrieverActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity buildChainBrokeredOrganizations2OrganizationPaymentsReportingActivityStub() {
    return Workflow.newActivityStub(ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE_LOCAL_ACTIVITY,
        this));
  }
}
