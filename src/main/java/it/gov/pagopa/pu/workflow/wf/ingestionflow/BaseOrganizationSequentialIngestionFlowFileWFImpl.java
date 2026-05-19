package it.gov.pagopa.pu.workflow.wf.ingestionflow;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.Constants;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public abstract class BaseOrganizationSequentialIngestionFlowFileWFImpl<T extends IngestionFlowFileResult> extends BaseIngestionFlowFileWFImpl<T> {

  private static final Duration SLEEP_BETWEEN_ACQUIRE_LOCK = Duration.ofSeconds(5);
  /**
   * The lock acquire max attempts before to clear Temporal history.
   * The threshold is very high ({@link Constants#THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW}), lock acquire is the first activity called, we are not interested on WF history, we will clear it before real limit
   */
  private static final int LOCK_ATTEMPTS_BEFORE_CLEAN_WF_HISTORY = 1000;

  @Override
  protected void setProcessingStatus(Long ingestionFlowFileId) {
    log.info("Acquiring lock for ingestionFlowFileId {}", ingestionFlowFileId);
    acquireLock(ingestionFlowFileId);
    log.info("Lock successfully acquired for ingestionFlowFileId {}", ingestionFlowFileId);
  }

  private void acquireLock(Long ingestionFlowFileId) {
    int attemptCounter = 0;
    while (!getIngestionFlowFileProcessingLockerActivity().acquireIngestionFlowFileProcessingLock(ingestionFlowFileId)) {
      attemptCounter++;

      if (attemptCounter >= LOCK_ATTEMPTS_BEFORE_CLEAN_WF_HISTORY) {
        log.info("Max attempts reached, continuing as new for ingestionFlowFileId {}",
          ingestionFlowFileId);
        Workflow.continueAsNew(ingestionFlowFileId);
      }

      log.info("Lock not acquired, retrying for ingestionFlowFileId {}", ingestionFlowFileId);
      Workflow.sleep(SLEEP_BETWEEN_ACQUIRE_LOCK);
    }
  }

  /** Returns the activity stub for acquiring the processing lock */
  protected abstract IngestionFlowFileProcessingLockerActivity getIngestionFlowFileProcessingLockerActivity();
}
