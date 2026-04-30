package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.csv.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.receipt.ReceiptIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.csv.config.ReceiptIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class ReceiptIngestionWFImpl extends BaseIngestionFlowFileWFImpl<ReceiptIngestionFlowFileResult> implements ReceiptIngestionWF {

  @Override
  protected Function<Long, ReceiptIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    ReceiptIngestionWfConfig wfConfig = applicationContext.getBean(ReceiptIngestionWfConfig.class);
    return wfConfig.buildReceiptIngestionActivityStub()::processFile;
  }
}
