package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaIngestionActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaNotifySilActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.receipt.ReceiptPagopaSendEmailActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.receipt.ReceiptPagopaIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.config.ReceiptPagopaIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class ReceiptPagopaIngestionWFImpl extends BaseIngestionFlowFileWFImpl<ReceiptPagopaIngestionFlowFileResult> implements ReceiptPagopaIngestionWF {

  private ReceiptPagopaNotifySilActivity receiptPagopaNotifySilActivity;
  private ReceiptPagopaSendEmailActivity receiptPagopaSendEmailActivity;

  @Override
  protected Function<Long, ReceiptPagopaIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    ReceiptPagopaIngestionWfConfig wfConfig = applicationContext.getBean(ReceiptPagopaIngestionWfConfig.class);

    ReceiptPagopaIngestionActivity receiptPagopaIngestionActivity = wfConfig.buildReceiptPagopaIngestionActivityStub();
    receiptPagopaNotifySilActivity = wfConfig.buildReceiptPagopaNotifySilActivityStub();
    receiptPagopaSendEmailActivity = wfConfig.buildReceiptPagopaSendEmailActivityStub();

    return receiptPagopaIngestionActivity::processFile;
  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId, ReceiptPagopaIngestionFlowFileResult result) {
    try {
      receiptPagopaNotifySilActivity.notifyReceiptToSil(result.getReceiptDTO());
    } catch (Exception e) {
      log.error("Error in notify SIL for receipt id[{}] loaded with ingestionFlowFileId[{}]", result.getReceiptDTO().getReceiptId(), ingestionFlowFileId, e);
    }
    receiptPagopaSendEmailActivity.sendReceiptHandledEmail(result.getReceiptDTO());
  }

  @Override
  protected void sendEmail(Long ingestionFlowFileId, boolean success) {
    // Do Nothing
  }
}
