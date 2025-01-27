package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.AcaStandInCreateDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-creation")
public class CreateDebtPositionWfConfig extends BaseWfConfig {

  public SendDebtPositionIONotificationActivity buildSendDebtPositionIONotificationActivityStub() {
    return Workflow.newActivityStub(SendDebtPositionIONotificationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public AcaStandInCreateDebtPositionActivity buildAcaStandInCreateDebtPositionActivityStub() {
    return Workflow.newActivityStub(AcaStandInCreateDebtPositionActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public FinalizeDebtPositionSyncStatusActivity buildFinalizeDebtPositionSyncStatusActivityStub() {
    return Workflow.newActivityStub(FinalizeDebtPositionSyncStatusActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }
}
