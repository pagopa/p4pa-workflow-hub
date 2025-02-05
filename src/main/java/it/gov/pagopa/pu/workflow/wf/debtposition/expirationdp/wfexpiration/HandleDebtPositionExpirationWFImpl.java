package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.DebtPositionExpirationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.HandleDebtPositionExpirationWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF;


@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF)
public class HandleDebtPositionExpirationWFImpl implements HandleDebtPositionExpirationWF, ApplicationContextAware {

  public static final String TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF = "HandleDebtPositionExpirationWF";

  private DebtPositionExpirationActivity debtPositionExpirationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    HandleDebtPositionExpirationWfConfig wfConfig = applicationContext.getBean(HandleDebtPositionExpirationWfConfig.class);
    debtPositionExpirationActivity = wfConfig.buildDebtPositionExpirationActivityStub();
  }

  @Override
  public OffsetDateTime handleDpExpiration(DebtPositionDTO debtPosition) {
    log.info("Starting workflow to handle expiration of DebtPosition with ID: {}", debtPosition.getDebtPositionId());
    OffsetDateTime nextDueDate = debtPositionExpirationActivity.checkAndUpdateInstallmentExpiration(debtPosition.getDebtPositionId());
    log.info("Handled expiration of DebtPosition with ID: {}", debtPosition.getDebtPositionId());
    return nextDueDate;
  }
}
