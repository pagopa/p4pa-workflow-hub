package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

/**
 * Workflow implementation for the Payments Reporting Ingestion Workflow
 */

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class PaymentsReportingIngestionWFImpl implements PaymentsReportingIngestionWF, ApplicationContextAware {
  public static final String TASK_QUEUE = "PaymentsReportingIngestionWF";

  private PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivity;
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity;
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;
  private NotifyPaymentsReportingToIufClassificationActivity notifyPaymentsReportingToIufClassificationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    PaymentsReportingIngestionWfConfig wfConfig = applicationContext.getBean(PaymentsReportingIngestionWfConfig.class);

    paymentsReportingIngestionFlowFileActivity = wfConfig.buildPaymentsReportingIngestionFlowFileActivityStub();
    sendEmailIngestionFlowActivity = wfConfig.buildSendEmailIngestionFlowActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();
    notifyPaymentsReportingToIufClassificationActivity = wfConfig.buildNotifyPaymentsReportingToIufClassificationActivityStub();
  }

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Handling PaymentsReporting IngestingFlowFileId {}", ingestionFlowFileId);

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    String errorDescription = processFile(ingestionFlowFileId);

    boolean success = errorDescription==null;
    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, success);
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      success
        ? IngestionFlowFile.StatusEnum.COMPLETED
        : IngestionFlowFile.StatusEnum.ERROR,
      errorDescription,
      null);

    log.info("PaymentsReporting Ingestion completed for file with ID {}", ingestionFlowFileId);
  }

  private String processFile(Long ingestionFlowFileId) {
    try{
      PaymentsReportingIngestionFlowFileActivityResult ingestionResult = paymentsReportingIngestionFlowFileActivity.processFile(ingestionFlowFileId);

      notifyPaymentsReportingToIufClassificationActivity.signalIufClassificationWithStart(
        ingestionResult.getOrganizationId(),
        ingestionResult.getIuf(),
        ingestionResult.getTransfers());

      return null;
    } catch (Exception e) {
      return e.getMessage();
    }
  }
}
