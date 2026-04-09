package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.debtposition.massive.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.activity.ScheduleToSyncMassiveIbanUpdateWFActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.config.MassiveDebtPositionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY)
public class MassiveIbanUpdateWFImpl implements MassiveIbanUpdateWF, ApplicationContextAware {

  private MassiveIbanUpdateActivity massiveIbanUpdateActivity;
  private ScheduleToSyncMassiveIbanUpdateWFActivity scheduleToSyncMassiveIbanUpdateWFActivity;

  private int loopExecutionCount = 0;

  private static final int WAITING_SECONDS_BEFORE_RETRY = 5 * 60;
  private static final int LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 100;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    MassiveDebtPositionWFConfig wfConfig = applicationContext.getBean(MassiveDebtPositionWFConfig.class);

    massiveIbanUpdateActivity = wfConfig.buildMassiveIbanUpdateActivityStub();
    scheduleToSyncMassiveIbanUpdateWFActivity = wfConfig.buildScheduleToSyncMassiveIbanUpdateWFActivityStub();
  }

  @Override
  public void massiveIbanUpdate(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban) {
    log.info("Start MassiveIbanUpdate Workflow for debtPositionTypeOrgId {} or organizationId {}", dptoId, orgId);

    String workflowId = Workflow.getInfo().getWorkflowId();

    if (!workflowId.contains("TO_SYNC")) {
      boolean isToSchedule = massiveIbanUpdateActivity.massiveIbanUpdateRetrieveAndUpdateDp(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

      if (isToSchedule) {
        scheduleToSyncMassiveIbanUpdateWFActivity.scheduleToSyncMassiveIbanUpdateWF(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);
      }

      return;
    }

    boolean isToRetry;
    do {
      isToRetry = massiveIbanUpdateActivity.massiveIbanUpdateRetrieveAndUpdateDp(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

      if (isToRetry) {
        waitForNextIteration(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);
      }
    } while(isToRetry);
  }

  private void waitForNextIteration(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban) {
    Workflow.sleep(
      Duration.of(
        WAITING_SECONDS_BEFORE_RETRY,
        ChronoUnit.SECONDS
      )
    );
    loopExecutionCount += 1;
    if(loopExecutionCount >= LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY) {
      loopExecutionCount = 0;
      Workflow.continueAsNew(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);
    }
  }
}
