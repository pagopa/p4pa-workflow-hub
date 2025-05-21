package it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.organization.OrganizationIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

  @Configuration
  @ConfigurationProperties(prefix = "workflow.payments-reporting-ingestion")
  public class OrganizationIngestionWFConfig extends BaseWfConfig {

    public OrganizationIngestionActivity buildOrganizationIngestionActivityStub() {
      return Workflow.newActivityStub(OrganizationIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
    }

    public UpdateIngestionFlowStatusActivity buildUpdateIngestionFlowStatusActivityStub() {
      return Workflow.newActivityStub(UpdateIngestionFlowStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
    }

    public SendEmailIngestionFlowActivity buildSendEmailIngestionFlowActivityStub() {
      return Workflow.newActivityStub(SendEmailIngestionFlowActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
    }
    }
