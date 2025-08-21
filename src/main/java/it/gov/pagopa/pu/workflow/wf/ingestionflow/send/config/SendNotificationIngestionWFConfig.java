package it.gov.pagopa.pu.workflow.wf.ingestionflow.send.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.sendnotification.SendNotificationIngestionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.send-notification-ingestion")
public class SendNotificationIngestionWFConfig extends BaseWfConfig {

  public SendNotificationIngestionActivity buildSendNotificationIngestionActivityStub() {
    return Workflow.newActivityStub(SendNotificationIngestionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
