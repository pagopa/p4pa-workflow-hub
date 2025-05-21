package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.MassiveNoticeGenerationStatusRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.SyncIngestedDebtPositionDTO;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.utilities.Constants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.Duration;

@Slf4j
@WorkflowImpl(taskQueues = DebtPositionIngestionFlowWFImpl.TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW)
public class DebtPositionIngestionFlowWFImpl implements DebtPositionIngestionFlowWF, ApplicationContextAware {
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
  private InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivity;
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity;
  private SynchronizeIngestedDebtPositionActivity synchronizeIngestedDebtPositionActivity;
  private MassiveNoticeGenerationStatusRetrieverActivity massiveNoticeGenerationStatusRetrieverActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DebtPositionIngestionFlowWfConfig wfConfig = applicationContext.getBean(DebtPositionIngestionFlowWfConfig.class);

    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    installmentIngestionFlowFileActivity = wfConfig.buildInstallmentIngestionFlowFileActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();
    sendEmailIngestionFlowActivity = wfConfig.buildSendEmailIngestionFlowActivityStub();
    synchronizeIngestedDebtPositionActivity = wfConfig.buildSynchronizeIngestedDebtPositionActivityStub();
    massiveNoticeGenerationStatusRetrieverActivity = wfConfig.buildMassiveNoticeGenerationStatusRetrieverActivity();
  }

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Acquiring lock for ingestionFlowFileId {}", ingestionFlowFileId);
    acquireLock(ingestionFlowFileId);

    log.info("Lock successfully acquired for ingestionFlowFileId {}", ingestionFlowFileId);
    InstallmentIngestionFlowFileResult ingestionResult = processFile(ingestionFlowFileId);

    SyncIngestedDebtPositionDTO syncDpResult
      = synchronizeIngestedDebtPositionActivity.synchronizeIngestedDebtPosition(ingestionFlowFileId);

    mergeErrorDescriptions(ingestionResult, syncDpResult.getErrorsDescription());
    boolean success = ingestionResult.getErrorDescription() == null;

    if (StringUtils.isNotBlank(syncDpResult.getPdfGeneratedId()) && success) {
      retrieveNoticesGenerationStatus(ingestionResult, syncDpResult.getPdfGeneratedId());
    }

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      IngestionFlowFileStatus.PROCESSING,
      success
        ? IngestionFlowFileStatus.COMPLETED
        : IngestionFlowFileStatus.ERROR,
      ingestionResult);
    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, success);

    log.info("Debt Position ingestion with ID {} is completed, with success {} and error_description {}",
      ingestionFlowFileId, success, ingestionResult.getErrorDescription());
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

  private InstallmentIngestionFlowFileResult processFile(Long ingestionFlowFileId) {
    InstallmentIngestionFlowFileResult ingestionResult;
    try {
      ingestionResult = installmentIngestionFlowFileActivity.processFile(ingestionFlowFileId);
    } catch (Exception e) {
      String error = "Unexpected error when processing DebtPositionIngestion file: " + Utilities.getWorkflowExceptionMessage(e);
      log.error(error);
      ingestionResult = InstallmentIngestionFlowFileResult.builder()
        .errorDescription(error)
        .build();
    }
    return ingestionResult;
  }

  private void mergeErrorDescriptions(IngestionFlowFileResult ingestionResult, String additionalError) {
    if (!StringUtils.isEmpty(additionalError)) {
      String ingestionResultErrorDescription = ingestionResult.getErrorDescription();
      ingestionResult.setErrorDescription(
        (ingestionResultErrorDescription == null ? "" : ingestionResultErrorDescription + "\n\n") +
          "There were errors during the synchronization of the ingested Debt Position:" + additionalError);
    }
  }

  private void retrieveNoticesGenerationStatus(InstallmentIngestionFlowFileResult ingestionResult, String pdfGeneratedId) {
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
      mergeErrorDescriptions(ingestionResult, errorMessage);
    } else {
      log.info("Generation status retrieved for pdfGeneratedId {}", pdfGeneratedId);
    }
  }
}
