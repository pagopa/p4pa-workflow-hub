package it.gov.pagopa.pu.workflow.wf.debtposition.sync.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.aca.SynchronizeInstallmentAcaActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.gpd.SynchronizeInstallmentGpdActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.gpdpreload.SynchronizeInstallmentGpdPreLoadActivity;
import it.gov.pagopa.pu.workflow.config.temporal.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.CancelCheckDpExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.StartIONotificationWFActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.ScheduleCheckDpExpirationActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-synchronize")
public class SynchronizeDebtPositionWfConfig extends BaseWfConfig {

  public FinalizeDebtPositionSyncStatusActivity buildFinalizeDebtPositionSyncStatusActivityStub() {
    return Workflow.newActivityStub(FinalizeDebtPositionSyncStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PublishPaymentEventActivity buildPublishPaymentEventActivityStub() {
    return Workflow.newActivityStub(PublishPaymentEventActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC_LOCAL, this));
  }

  public CancelCheckDpExpirationScheduleActivity buildCancelCheckDpExpirationScheduleActivityStub() {
    return Workflow.newActivityStub(CancelCheckDpExpirationScheduleActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC_LOCAL, this));
  }

  public ScheduleCheckDpExpirationActivity buildScheduleCheckDpExpirationActivityStub() {
    return Workflow.newActivityStub(ScheduleCheckDpExpirationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC_LOCAL,
      this));
  }

  public SynchronizeInstallmentAcaActivity buildSynchronizeInstallmentAcaActivity() {
    return Workflow.newActivityStub(SynchronizeInstallmentAcaActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SynchronizeInstallmentGpdActivity buildSynchronizeInstallmentGpdActivity() {
    return Workflow.newActivityStub(SynchronizeInstallmentGpdActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public SynchronizeInstallmentGpdPreLoadActivity buildSynchronizeInstallmentGpdPreLoadActivity() {
    return Workflow.newActivityStub(SynchronizeInstallmentGpdPreLoadActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public StartIONotificationWFActivity buildInvokeIONotificationActivityStub() {
    return Workflow.newActivityStub(StartIONotificationWFActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC_LOCAL,
      this));
  }
}
