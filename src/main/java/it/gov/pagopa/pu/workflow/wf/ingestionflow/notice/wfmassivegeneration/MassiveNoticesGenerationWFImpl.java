package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.notice.FetchAndMergeNoticesActivity;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity.ScheduleMassiveNoticesFileDeletionWFActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.config.MassiveNoticesGenerationWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class MassiveNoticesGenerationWFImpl implements MassiveNoticesGenerationWF, ApplicationContextAware {
  private static final int LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY = 100;
  private static final int WAITING_SECONDS_NEXT_ITERATION = 60 * 2;
  private static final int DAYS_TO_KEEP_FILE = 100;

  private int loopExecutionCount = 0;

  private FetchAndMergeNoticesActivity fetchAndMergeNoticesActivity;
  private ScheduleMassiveNoticesFileDeletionWFActivity scheduleMassiveNoticesFileDeletionWFActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    MassiveNoticesGenerationWFConfig wfConfig = applicationContext.getBean(MassiveNoticesGenerationWFConfig.class);
    this.fetchAndMergeNoticesActivity = wfConfig.buildFetchAndMergeNoticesActivityStub();
    this.scheduleMassiveNoticesFileDeletionWFActivity = wfConfig.buildScheduleMassiveNoticesFileDeletionWFActivityStub();
  }

  @Override
  public void generate(Long ingestionFlowFileId) {
    log.info("Starting massive notices generation wf for ingestionFlowFileId {}", ingestionFlowFileId);

    int result;

    do {
      result = fetchAndMergeNoticesActivity.fetchAndMergeNotices(ingestionFlowFileId);

      if (result == 0) {
        waitForNextIteration(ingestionFlowFileId);
      }
    } while (result == 0);

    long currentTimeMillis = Workflow.currentTimeMillis();

    LocalDate scheduleDate = Instant.ofEpochMilli(currentTimeMillis)
      .atZone(ZoneId.systemDefault())
      .toLocalDate()
      .plusDays(DAYS_TO_KEEP_FILE);

    scheduleMassiveNoticesFileDeletionWFActivity.scheduleFileDeletion(ingestionFlowFileId, scheduleDate);
  }

  private void waitForNextIteration(Long ingestionFlowFileId) {
    Workflow.sleep(
      Duration.of(
        WAITING_SECONDS_NEXT_ITERATION,
        ChronoUnit.SECONDS
      )
    );
    loopExecutionCount += 1;
    if (loopExecutionCount >= LOOP_EXECUTIONS_BEFORE_CLEAN_WF_HISTORY) {
      loopExecutionCount = 0;
      Workflow.continueAsNew(ingestionFlowFileId);
    }
  }
}
