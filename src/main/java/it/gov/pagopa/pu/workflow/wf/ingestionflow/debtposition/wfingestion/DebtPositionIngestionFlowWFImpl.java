package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
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

  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivity;
  private InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivity;
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DebtPositionIngestionFlowWfConfig wfConfig = applicationContext.getBean(DebtPositionIngestionFlowWfConfig.class);

    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    installmentIngestionFlowFileActivity = wfConfig.buildInstallmentIngestionFlowFileActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();
    sendEmailIngestionFlowActivity = wfConfig.buildSendEmailIngestionFlowActivityStub();
  }

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Acquiring lock for ingestionFlowFileId {}", ingestionFlowFileId);

    int attemptCounter = 0;
//    int maxAttemptsBeforeContinueAsNew = 5000;
    while (!ingestionFlowFileProcessingLockerActivity.acquireProcessingLock(ingestionFlowFileId)) {
      attemptCounter++;
// TODO: evaluate to continueAsNew the WF when activity is called too many times in order to avoid to incur on max events limit
//      if (attemptCounter >= maxAttemptsBeforeContinueAsNew) {
//        log.info("Max attempts reached, continuing as new for ingestionFlowFileId {}", ingestionFlowFileId);
//        Workflow.continueAsNew(ingestionFlowFileId);
//        return;
//      }

      log.info("Lock not acquired, retrying for ingestionFlowFileId {}", ingestionFlowFileId);
      Workflow.sleep(SLEEP_BETWEEN_ACQUIRE_LOCK);
    }

    log.info("Lock successfully acquired for ingestionFlowFileId {}", ingestionFlowFileId);
    InstallmentIngestionFlowFileResult ingestionResult = processFile(ingestionFlowFileId);
    boolean success = StringUtils.isEmpty(ingestionResult.getErrorDescription());

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      IngestionFlowFile.StatusEnum.PROCESSING,
      success
        ? IngestionFlowFile.StatusEnum.COMPLETED
        : IngestionFlowFile.StatusEnum.ERROR,
      ingestionResult.getErrorDescription(),
      ingestionResult.getDiscardedFileName());
    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, success);

    log.info("Debt Position ingestion with ID {} is completed, with success {} and error_description {}",
      ingestionFlowFileId, success, ingestionResult.getErrorDescription());
  }

  private InstallmentIngestionFlowFileResult processFile(Long ingestionFlowFileId) {
    InstallmentIngestionFlowFileResult ingestionResult;
    try {
      ingestionResult = installmentIngestionFlowFileActivity.processFile(ingestionFlowFileId);
    } catch (Exception e) {
      String error = "Unexpected error when processing DebtPositionIngestion file: " + e.getMessage();
      log.error(error);
      ingestionResult = new InstallmentIngestionFlowFileResult(
        null,
        null,
        error,
        null
      );
    }
    return ingestionResult;
  }
}
