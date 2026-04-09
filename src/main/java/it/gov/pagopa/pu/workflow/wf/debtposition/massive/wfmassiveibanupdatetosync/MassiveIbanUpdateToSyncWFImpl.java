package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdatetosync;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.massive.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.config.MassiveDebtPositionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY)
public class MassiveIbanUpdateToSyncWFImpl implements MassiveIbanUpdateToSyncWF, ApplicationContextAware {
  private MassiveIbanUpdateActivity massiveIbanUpdateActivity;

  private int loopExecutionCount = 0;

  private static final int WAITING_SECONDS_BEFORE_RETRY = 5 * 60;
  private static final int LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 100;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    MassiveDebtPositionWFConfig wfConfig = applicationContext.getBean(MassiveDebtPositionWFConfig.class);

    massiveIbanUpdateActivity = wfConfig.buildMassiveIbanUpdateActivityStub();
  }

  @Override
  public void massiveIbanUpdate(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban) {
    log.info("Start MassiveIbanUpdate Workflow TO_SYNC for debtPositionTypeOrgId {} or organizationId {}", dptoId, orgId);

    boolean isToRetry;
    do {
      isToRetry = massiveIbanUpdateActivity.massiveIbanUpdateRetrieveAndUpdateDp(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

      if (isToRetry) {
        waitForNextIteration(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);
      }
    } while (isToRetry);
  }

  private void waitForNextIteration(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban) {
    Workflow.sleep(
      Duration.of(
        WAITING_SECONDS_BEFORE_RETRY,
        ChronoUnit.SECONDS
      )
    );
    loopExecutionCount += 1;
    if (loopExecutionCount >= LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY) {
      loopExecutionCount = 0;
      Workflow.continueAsNew(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);
    }
  }
}
