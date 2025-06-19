package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class PaymentsReportingIngestionWFImpl extends BaseIngestionFlowFileWFImpl<PaymentsReportingIngestionFlowFileActivityResult> implements PaymentsReportingIngestionWF {

  private NotifyPaymentsReportingToIufClassificationActivity notifyPaymentsReportingToIufClassificationActivity;

  @Override
  protected Function<Long, PaymentsReportingIngestionFlowFileActivityResult> buildActivityStubs(ApplicationContext applicationContext) {
    PaymentsReportingIngestionWfConfig wfConfig = applicationContext.getBean(PaymentsReportingIngestionWfConfig.class);

    PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivity = wfConfig.buildPaymentsReportingIngestionFlowFileActivityStub();
    notifyPaymentsReportingToIufClassificationActivity = wfConfig.buildNotifyPaymentsReportingToIufClassificationActivityStub();

    return paymentsReportingIngestionFlowFileActivity::processFile;
  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId, PaymentsReportingIngestionFlowFileActivityResult result) {
    notifyPaymentsReportingToIufClassificationActivity.signalPaymentsReportingIufClassificationWithStart(
      result.getOrganizationId(),
      result.getIuf(),
      result.getTransfers());
  }
}
