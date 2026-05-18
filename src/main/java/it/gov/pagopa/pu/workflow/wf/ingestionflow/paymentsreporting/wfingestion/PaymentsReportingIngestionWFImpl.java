package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.utilities.Constants;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class PaymentsReportingIngestionWFImpl extends BaseIngestionFlowFileWFImpl<PaymentsReportingIngestionFlowFileActivityResult> implements PaymentsReportingIngestionWF {

  private static final Duration SLEEP_BETWEEN_ACQUIRE_LOCK = Duration.ofSeconds(5);
  /**
   * The lock acquire max attempts before to clear Temporal history.
   * The threshold is very high ({@link Constants#THRESHOLD_TEMPORAL_EVENTS_BEFORE_CONTINUE_AS_NEW}), lock acquire is the first activity called, we are not interested on WF history, we will clear it before real limit
   */
  private static final int LOCK_ATTEMPTS_BEFORE_CLEAN_WF_HISTORY = 1000;

  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivity;
  private NotifyPaymentsReportingToIufClassificationActivity notifyPaymentsReportingToIufClassificationActivity;

  @Override
  protected Function<Long, PaymentsReportingIngestionFlowFileActivityResult> buildActivityStubs(ApplicationContext applicationContext) {
    PaymentsReportingIngestionWfConfig wfConfig = applicationContext.getBean(PaymentsReportingIngestionWfConfig.class);

    PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivity = wfConfig.buildPaymentsReportingIngestionFlowFileActivityStub();
    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    notifyPaymentsReportingToIufClassificationActivity = wfConfig.buildNotifyPaymentsReportingToIufClassificationActivityStub();

    return paymentsReportingIngestionFlowFileActivity::processFile;
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
  protected void afterProcessing(Long ingestionFlowFileId, PaymentsReportingIngestionFlowFileActivityResult result) {
    notifyPaymentsReportingToIufClassificationActivity.signalPaymentsReportingIufClassificationWithStart(
      result.getOrganizationId(),
      result.getIuf(),
      result.getTransfers());
  }
}
