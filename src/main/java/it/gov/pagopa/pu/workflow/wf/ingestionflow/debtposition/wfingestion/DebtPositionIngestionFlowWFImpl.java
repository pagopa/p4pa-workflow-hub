package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.MassiveNoticeGenerationStatusRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.SyncIngestedDebtPositionDTO;
import it.gov.pagopa.pu.workflow.utilities.Constants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.time.Duration;

@Slf4j
@WorkflowImpl(taskQueues = DebtPositionIngestionFlowWFImpl.TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW)
public class DebtPositionIngestionFlowWFImpl extends BaseIngestionFlowFileWFImpl<InstallmentIngestionFlowFileResult> implements DebtPositionIngestionFlowWF {
  public static final String TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW = "DebtPositionIngestionFlowWF";

  private static final Duration SLEEP_BETWEEN_ACQUIRE_LOCK = Duration.ofSeconds(5);
  /**
   * The lock acquire max attempts before to clear Temporal history.
   * The threshold is very high ({@link Constants#THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW}), lock acquire is the first activity called, we are not interested on WF history, we will clear it before real limit
   */
  private static final int LOCK_ATTEMPTS_BEFORE_CLEAN_WF_HISTORY = 1000;
  private static final int MAX_ATTEMPTS = 30;
  private static final Duration RETRY_INTERVAL = Duration.ofSeconds(30);

  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivity;
  private SynchronizeIngestedDebtPositionActivity synchronizeIngestedDebtPositionActivity;
  private MassiveNoticeGenerationStatusRetrieverActivity massiveNoticeGenerationStatusRetrieverActivity;

  @Override
  protected InstallmentIngestionFlowFileActivity buildActivityStubs(ApplicationContext applicationContext) {
    DebtPositionIngestionFlowWfConfig wfConfig = applicationContext.getBean(DebtPositionIngestionFlowWfConfig.class);

    InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivity = wfConfig.buildInstallmentIngestionFlowFileActivityStub();
    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    synchronizeIngestedDebtPositionActivity = wfConfig.buildSynchronizeIngestedDebtPositionActivityStub();
    massiveNoticeGenerationStatusRetrieverActivity = wfConfig.buildMassiveNoticeGenerationStatusRetrieverActivity();

    return installmentIngestionFlowFileActivity;
  }

  @Override
  protected void setProcessingStatus(Long ingestionFlowFileId) {
    log.info("Acquiring lock for ingestionFlowFileId {}", ingestionFlowFileId);
    acquireLock(ingestionFlowFileId);
    log.info("Lock successfully acquired for ingestionFlowFileId {}", ingestionFlowFileId);
  }

  private void acquireLock(Long ingestionFlowFileId) {
    int attemptCounter = 0;
    while (!ingestionFlowFileProcessingLockerActivity.acquireProcessingLock(ingestionFlowFileId)) {
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
      retrieveNoticesGenerationStatus(ingestionResult, syncDpResult.getPdfGeneratedId());
    }
  }

  private void mergeErrorDescriptions(IngestionFlowFileResult ingestionResult, String phase, String additionalError) {
    if (!StringUtils.isEmpty(additionalError)) {
      String ingestionResultErrorDescription = ingestionResult.getErrorDescription();
      ingestionResult.setErrorDescription(
        (ingestionResultErrorDescription == null ? "" : ingestionResultErrorDescription + "\n\n") +
          "There were errors during the " + phase + " of the ingested Debt Position:" + additionalError);
    }
  }

  private void retrieveNoticesGenerationStatus(IngestionFlowFileResult ingestionResult, String pdfGeneratedId) {
    int attemptCounter = 0;
    while (attemptCounter < MAX_ATTEMPTS &&
      massiveNoticeGenerationStatusRetrieverActivity.retrieveNoticesGenerationStatus(ingestionResult.getOrganizationId(), pdfGeneratedId) == null) {
      attemptCounter++;

      log.info("Generation status not retrieved, retrying for pdfGeneratedId {} (attempt {})", pdfGeneratedId, attemptCounter);
      Workflow.sleep(RETRY_INTERVAL);
    }

    if (attemptCounter >= MAX_ATTEMPTS) {
      String errorMessage = String.format("Max attempts reached for pdfGeneratedId %s. Unable to retrieve generation status.", pdfGeneratedId);
      log.error(errorMessage);
      mergeErrorDescriptions(ingestionResult, "notice generation", errorMessage);
    } else {
      log.info("Generation status retrieved for pdfGeneratedId {}", pdfGeneratedId);
    }
  }
}
