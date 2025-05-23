package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaIngestionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaNotifySilActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaSendEmailActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.receipt.ReceiptPagopaIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.config.ReceiptPagopaIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
@WorkflowImpl(taskQueues = ReceiptPagopaIngestionWFImpl.TASK_QUEUE_RECEIPT_PAGOPA_INGESTION_WF)
public class ReceiptPagopaIngestionWFImpl extends BaseIngestionFlowFileWFImpl<ReceiptPagopaIngestionFlowFileResult> implements ReceiptPagopaIngestionWF {
  public static final String TASK_QUEUE_RECEIPT_PAGOPA_INGESTION_WF = "ReceiptPagopaIngestionWF";

  private ReceiptPagopaNotifySilActivity receiptPagopaNotifySilActivity;
  private ReceiptPagopaSendEmailActivity receiptPagopaSendEmailActivity;

  @Override
  protected ReceiptPagopaIngestionActivity buildActivityStubs(ApplicationContext applicationContext) {
    ReceiptPagopaIngestionWfConfig wfConfig = applicationContext.getBean(ReceiptPagopaIngestionWfConfig.class);

    ReceiptPagopaIngestionActivity receiptPagopaIngestionActivity = wfConfig.buildReceiptPagopaIngestionActivityStub();
    receiptPagopaNotifySilActivity = wfConfig.buildReceiptPagopaNotifySilActivityStub();
    receiptPagopaSendEmailActivity = wfConfig.buildReceiptPagopaSendEmailActivityStub();

    return receiptPagopaIngestionActivity;
  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId, ReceiptPagopaIngestionFlowFileResult result) {
    try {
      receiptPagopaNotifySilActivity.handleNotifySil(result.getReceiptDTO(), result.getInstallmentDTO());
    } catch (Exception e) {
      log.error("Error in notify SIL for receipt id[{}]", ingestionFlowFileId, e);
    }
    receiptPagopaSendEmailActivity.sendEmail(result.getReceiptDTO(), result.getInstallmentDTO());
  }

  @Override
  protected void sendEmail(Long ingestionFlowFileId, boolean success) {
    // Do Nothing
  }
}
