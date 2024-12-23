package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.config.CreateDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWFImpl.TASK_QUEUE;


@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class CreateDebtPositionSyncWFImpl implements CreateDebtPositionSyncWF, ApplicationContextAware {

  public static final String TASK_QUEUE = "CreateDebtPositionWf";

  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CreateDebtPositionWfConfig wfConfig = applicationContext.getBean(CreateDebtPositionWfConfig.class);
    sendDebtPositionIONotificationActivity = wfConfig.buildSendDebtPositionIONotificationActivityStub();
  }

  @Override
  public void createDPSync(DebtPositionDTO debtPosition) {
    log.info("Starting workflow for ingesting DebtPosition with ID: {}", debtPosition.getDebtPositionId());
    sendDebtPositionIONotificationActivity.sendMessage(debtPosition);
    log.info("Message to IO sent with payload {}", debtPosition);
  }
}
