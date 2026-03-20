package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.SyncIngestedDebtPositionDTO;
import it.gov.pagopa.pu.workflow.utilities.Constants;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.activity.StartMassiveNoticesGenerationWFActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class DebtPositionIngestionFlowWFImpl extends BaseIngestionFlowFileWFImpl<InstallmentIngestionFlowFileResult> implements DebtPositionIngestionFlowWF {

  private static final Duration SLEEP_BETWEEN_ACQUIRE_LOCK = Duration.ofSeconds(5);
  /**
   * The lock acquire max attempts before to clear Temporal history.
   * The threshold is very high ({@link Constants#THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW}), lock acquire is the first activity called, we are not interested on WF history, we will clear it before real limit
   */
  private static final int LOCK_ATTEMPTS_BEFORE_CLEAN_WF_HISTORY = 1000;

  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivity;
  private SynchronizeIngestedDebtPositionActivity synchronizeIngestedDebtPositionActivity;
  private StartMassiveNoticesGenerationWFActivity startMassiveNoticesGenerationWFActivity;

  @Override
  protected Function<Long, InstallmentIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    DebtPositionIngestionFlowWfConfig wfConfig = applicationContext.getBean(DebtPositionIngestionFlowWfConfig.class);

    InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivity = wfConfig.buildInstallmentIngestionFlowFileActivityStub();
    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    synchronizeIngestedDebtPositionActivity = wfConfig.buildSynchronizeIngestedDebtPositionActivityStub();
    startMassiveNoticesGenerationWFActivity = wfConfig.buildStartMassiveNoticesGenerationWFActivityStub();

    return installmentIngestionFlowFileActivity::processFile;
  }

  @Override
  protected void setProcessingStatus(Long ingestionFlowFileId) {
    log.info("Acquiring lock for ingestionFlowFileId {}", ingestionFlowFileId);
    acquireLock(ingestionFlowFileId);
    log.info("Lock successfully acquired for ingestionFlowFileId {}", ingestionFlowFileId);
  }

  private void acquireLock(Long ingestionFlowFileId) {
    int attemptCounter = 0;
    while (!ingestionFlowFileProcessingLockerActivity.acquireIngestionFlowFileProcessingLock(ingestionFlowFileId)) {
      attemptCounter++;

      if (attemptCounter >= LOCK_ATTEMPTS_BEFORE_CLEAN_WF_HISTORY) {
        log.info("Max attempts reached, continuing as new for ingestionFlowFileId {}", ingestionFlowFileId);
        Workflow.continueAsNew(ingestionFlowFileId);
      }

      log.info("Lock not acquired, retrying for ingestionFlowFileId {}", ingestionFlowFileId);
      Workflow.sleep(SLEEP_BETWEEN_ACQUIRE_LOCK);
    }
  }

  @Override
  public void finallyAfterProcessing(Long ingestionFlowFileId, IngestionFlowFileResult ingestionResult) {
    SyncIngestedDebtPositionDTO syncDpResult
      = synchronizeIngestedDebtPositionActivity.synchronizeIngestedDebtPosition(ingestionFlowFileId);

    mergeErrorDescriptions(ingestionResult, "synchronization", syncDpResult.getErrorsDescription());
    boolean success = ingestionResult.getErrorDescription() == null;

    if (StringUtils.isNotBlank(syncDpResult.getPdfGeneratedId()) && success) {
      startMassiveNoticesGenerationWFActivity.startMassiveNoticesGenerationWF(ingestionFlowFileId);
    }
  }
}
