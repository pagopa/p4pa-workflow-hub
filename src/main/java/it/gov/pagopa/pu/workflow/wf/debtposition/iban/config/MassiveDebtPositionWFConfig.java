package it.gov.pagopa.pu.workflow.wf.debtposition.iban.config;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.iban.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.activity.ScheduleToSyncMassiveIbanUpdateWFActivity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@EqualsAndHashCode(callSuper = true)
@Configuration
@ConfigurationProperties(prefix = "workflow.massive-debt-position")
public class MassiveDebtPositionWFConfig extends BaseWfConfig {
  private int heartbeatTimeoutInSeconds;

 public MassiveIbanUpdateActivity buildMassiveIbanUpdateActivityStub() {
   ActivityOptions activityOptions = ActivityOptions.newBuilder(TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this))
     .setHeartbeatTimeout(Duration.ofSeconds(heartbeatTimeoutInSeconds))
     .build();

   return Workflow.newActivityStub(MassiveIbanUpdateActivity.class, activityOptions);
 }

  public ScheduleToSyncMassiveIbanUpdateWFActivity buildScheduleToSyncMassiveIbanUpdateWFActivityStub() {
    return Workflow.newActivityStub(ScheduleToSyncMassiveIbanUpdateWFActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY_LOCAL,
      this));
  }
}
