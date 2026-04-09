package it.gov.pagopa.pu.workflow.wf.debtposition.massive.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.massive.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.activity.ScheduleToSyncMassiveIbanUpdateWFActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.massive-debt-position")
public class MassiveDebtPositionWFConfig extends BaseWfConfig {

 public MassiveIbanUpdateActivity buildMassiveIbanUpdateActivityStub() {
   return Workflow.newActivityStub(MassiveIbanUpdateActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
 }

  public ScheduleToSyncMassiveIbanUpdateWFActivity buildScheduleToSyncMassiveIbanUpdateWFActivityStub() {
    return Workflow.newActivityStub(ScheduleToSyncMassiveIbanUpdateWFActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY_LOCAL,
      this));
  }
}
