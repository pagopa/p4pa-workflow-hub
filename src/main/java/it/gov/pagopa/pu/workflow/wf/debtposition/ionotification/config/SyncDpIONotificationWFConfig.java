package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.IONotificationDebtPositionActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-io-notification")
public class SyncDpIONotificationWFConfig extends BaseWfConfig {

  public IONotificationDebtPositionActivity buildIoNotificationDebtPositionActivityStub() {
    return Workflow.newActivityStub(IONotificationDebtPositionActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PublishPaymentEventActivity buildPublishPaymentEventActivityStub() {
    return Workflow.newActivityStub(PublishPaymentEventActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC_LOCAL, this));
  }
}
