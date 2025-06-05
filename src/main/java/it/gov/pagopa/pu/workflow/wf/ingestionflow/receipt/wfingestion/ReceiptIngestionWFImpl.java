package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.receipt.ReceiptIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.config.ReceiptIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = ReceiptIngestionWFImpl.TASK_QUEUE_RECEIPT_INGESTION_WF)
public class ReceiptIngestionWFImpl extends BaseIngestionFlowFileWFImpl<ReceiptIngestionFlowFileResult> implements ReceiptIngestionWF {
  public static final String TASK_QUEUE_RECEIPT_INGESTION_WF = "ReceiptIngestionWF";

  @Override
  protected Function<Long, ReceiptIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    ReceiptIngestionWfConfig wfConfig = applicationContext.getBean(ReceiptIngestionWfConfig.class);
    return wfConfig.buildReceiptIngestionActivityStub()::processFile;
  }
}
