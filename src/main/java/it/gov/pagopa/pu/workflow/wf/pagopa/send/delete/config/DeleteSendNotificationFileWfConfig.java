package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.delete.DeleteSendNotificationFileActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.send-notification-delete")
public class DeleteSendNotificationFileWfConfig extends BaseWfConfig {

  public DeleteSendNotificationFileActivity buildDeleteSendNotificationFileActivityStub() {
    return Workflow.newActivityStub(DeleteSendNotificationFileActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
