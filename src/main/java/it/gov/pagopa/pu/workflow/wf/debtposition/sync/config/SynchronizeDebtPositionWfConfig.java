package it.gov.pagopa.pu.workflow.wf.debtposition.sync.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.SynchronizeInstallmentAcaActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-synchronize")
public class SynchronizeDebtPositionWfConfig extends BaseWfConfig {
  public static final String TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY = "SynchronizeDP_LOCAL";

  public SynchronizeInstallmentAcaActivity buildSynchronizeInstallmentAcaActivity() {
    return Workflow.newActivityStub(SynchronizeInstallmentAcaActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public FinalizeDebtPositionSyncStatusActivity buildFinalizeDebtPositionSyncStatusActivityStub() {
    return Workflow.newActivityStub(FinalizeDebtPositionSyncStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public PublishPaymentEventActivity buildPublishPaymentEventActivityStub() {
    return Workflow.newActivityStub(PublishPaymentEventActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
        TASK_QUEUE_SYNCHRONIZE_DP_LOCAL_ACTIVITY, this));
  }

  public SendDebtPositionIONotificationActivity buildSendDebtPositionIONotificationActivityStub() {
    return Workflow.newActivityStub(SendDebtPositionIONotificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
