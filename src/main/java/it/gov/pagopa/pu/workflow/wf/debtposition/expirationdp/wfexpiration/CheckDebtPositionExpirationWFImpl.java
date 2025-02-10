package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.DebtPositionExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF;


@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF)
public class CheckDebtPositionExpirationWFImpl implements CheckDebtPositionExpirationWF, ApplicationContextAware {

  public static final String TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF = "CheckDebtPositionExpirationWF";
  public static final String TASK_QUEUE_SCHEDULE_CHECK_DP_EXPIRATION_LOCAL_ACTIVITY = "ScheduleCheckDebtPositionExpirationWF_LOCAL";

  private DebtPositionExpirationActivity debtPositionExpirationActivity;
  private ScheduleCheckDpExpirationActivity scheduleCheckDpExpirationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CheckDebtPositionExpirationWfConfig wfConfig = applicationContext.getBean(CheckDebtPositionExpirationWfConfig.class);
    debtPositionExpirationActivity = wfConfig.buildDebtPositionExpirationActivityStub();
    scheduleCheckDpExpirationActivity = wfConfig.buildScheduleCheckDpExpirationActivityStub();
  }

  @Override
  public void checkDpExpiration(Long debtPositionId) {
    log.info("Starting workflow to check expiration of DebtPosition with ID: {}", debtPositionId);
    OffsetDateTime nextDueDate = debtPositionExpirationActivity.checkAndUpdateInstallmentExpiration(debtPositionId);
    log.info("Checked expiration of DebtPosition with ID: {}, and retrieved the next due date: {}", debtPositionId, nextDueDate);
    if (nextDueDate != null) {
      log.info("Start scheduling the next check expiration of DebtPosition with ID: {}", debtPositionId);
      scheduleCheckDpExpirationActivity.scheduleNextCheckDpExpiration(debtPositionId, nextDueDate.plusDays(1));
    }
  }

}
