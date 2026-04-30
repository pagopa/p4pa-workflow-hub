package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.DebtPositionExpirationActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.LocalDate;


@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY)
public class CheckDebtPositionExpirationWFImpl implements CheckDebtPositionExpirationWF, ApplicationContextAware {

  private DebtPositionExpirationActivity debtPositionExpirationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CheckDebtPositionExpirationWfConfig wfConfig = applicationContext.getBean(CheckDebtPositionExpirationWfConfig.class);
    debtPositionExpirationActivity = wfConfig.buildDebtPositionExpirationActivityStub();
  }

  @Override
  public void checkDpExpiration(Long debtPositionId) {
    log.info("Starting workflow to check expiration of DebtPosition with ID: {}", debtPositionId);
    LocalDate nextDueDate = debtPositionExpirationActivity.checkAndUpdateInstallmentExpiration(debtPositionId);
    log.info("Checked expiration of DebtPosition with ID: {}, and retrieved the next due date: {}", debtPositionId, nextDueDate);
  }

}
