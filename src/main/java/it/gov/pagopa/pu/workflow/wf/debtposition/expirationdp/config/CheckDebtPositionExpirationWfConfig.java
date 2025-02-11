package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.DebtPositionExpirationActivity;
import it.gov.pagopa.pu.workflow.config.BaseWfConfig;
import it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWFImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow.debt-position-expiration")
public class CheckDebtPositionExpirationWfConfig extends BaseWfConfig {

  public DebtPositionExpirationActivity buildDebtPositionExpirationActivityStub() {
    return Workflow.newActivityStub(DebtPositionExpirationActivity.class,
      TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(this));
  }

  public ScheduleCheckDpExpirationActivity buildScheduleCheckDpExpirationActivityStub() {
    return Workflow.newActivityStub(ScheduleCheckDpExpirationActivity.class, TemporalWFImplementationCustomizer.baseWfConfig2ActivityOptions(
      CheckDebtPositionExpirationWFImpl.TASK_QUEUE_SCHEDULE_CHECK_DP_EXPIRATION_LOCAL_ACTIVITY,
      this));
  }
}
