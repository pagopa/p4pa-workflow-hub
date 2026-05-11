package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.delete.DeleteSendLegalFactFileActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.send-legal-fact-delete")
public class DeleteSendLegalFactFileWfConfig extends BaseWfConfig {

  public DeleteSendLegalFactFileActivity buildDeleteSendLegalFactFileActivityStub() {
    return Workflow.newActivityStub(DeleteSendLegalFactFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
