package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionFineReductionOptionExpirationActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionSynchronizeFineActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.CancelReductionExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity.ScheduleReductionExpirationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-fine")
public class DebtPositionFineWfConfig extends BaseWfConfig {

  public DebtPositionSynchronizeFineActivity buildDebtPositionSynchronizeFineActivityStub() {
    return Workflow.newActivityStub(DebtPositionSynchronizeFineActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public DebtPositionFineReductionOptionExpirationActivity buildDebtPositionFineReductionOptionExpirationActivityStub() {
    return Workflow.newActivityStub(DebtPositionFineReductionOptionExpirationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public InvokeSyncDebtPositionActivity buildInvokeSyncDebtPositionActivityStub(){
    return Workflow.newActivityStub(InvokeSyncDebtPositionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC_LOCAL,
      this));
  }

  public CancelReductionExpirationScheduleActivity buildCancelReductionExpirationScheduleActivityStub(){
    return Workflow.newActivityStub(CancelReductionExpirationScheduleActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC_LOCAL,
      this));
  }

  public ScheduleReductionExpirationActivity buildScheduleReductionExpirationActivityStub(){
    return Workflow.newActivityStub(ScheduleReductionExpirationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC_LOCAL,
      this));
  }
}
