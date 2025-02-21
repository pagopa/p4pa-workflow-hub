package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaIngestionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaNotifySilActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaSendEmailActivity;
import it.gov.pagopa.payhub.activities.dto.receipt.ReceiptPagopaIngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.config.ReceiptPagopaIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = ReceiptPagopaIngestionWFImpl.TASK_QUEUE_RECEIPT_PAGOPA_INGESTION_WF)
public class ReceiptPagopaIngestionWFImpl implements ReceiptPagopaIngestionWF, ApplicationContextAware {
  public static final String TASK_QUEUE_RECEIPT_PAGOPA_INGESTION_WF = "ReceiptPagopaIngestionWF";

  private ReceiptPagopaIngestionActivity receiptPagopaIngestionActivity;
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;
  private ReceiptPagopaNotifySilActivity receiptPagopaNotifySilActivity;
  private ReceiptPagopaSendEmailActivity receiptPagopaSendEmailActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ReceiptPagopaIngestionWfConfig wfConfig = applicationContext.getBean(ReceiptPagopaIngestionWfConfig.class);

    receiptPagopaIngestionActivity = wfConfig.buildReceiptPagopaIngestionActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();
    receiptPagopaNotifySilActivity = wfConfig.buildReceiptPagopaNotifySilActivityStub();
    receiptPagopaSendEmailActivity = wfConfig.buildReceiptPagopaSendEmailActivityStub();
  }

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Handling Receipt Pagopa ingestionFlowFileId {}", ingestionFlowFileId);

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.UPLOADED, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    ReceiptPagopaIngestionFlowFileResult ingestionResult = null;
    boolean success = true;
    String errorDescription = null;
    try {
      ingestionResult = receiptPagopaIngestionActivity.processFile(ingestionFlowFileId);
    } catch (Exception e) {
      log.error("Error processFile for receipt id[{}]", ingestionFlowFileId, e);
      success = false;
      errorDescription = "error processing receipt id[%s]: %s".formatted(ingestionFlowFileId, e.getMessage());
    }

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      IngestionFlowFile.StatusEnum.PROCESSING,
      success
        ? IngestionFlowFile.StatusEnum.COMPLETED
        : IngestionFlowFile.StatusEnum.ERROR,
      errorDescription,
      null);

    if (success) {
      try {
        receiptPagopaNotifySilActivity.handleNotifySil(ingestionResult.getReceiptDTO(), ingestionResult.getInstallmentDTO());
      } catch (Exception e) {
        log.error("Error in notify SIL for receipt id[{}]", ingestionFlowFileId, e);
      }

      receiptPagopaSendEmailActivity.sendEmail(ingestionResult.getReceiptDTO(), ingestionResult.getInstallmentDTO());
    }

    log.info("Receipt Pagopa ingestion with ID {} is completed", ingestionFlowFileId);
  }

}
