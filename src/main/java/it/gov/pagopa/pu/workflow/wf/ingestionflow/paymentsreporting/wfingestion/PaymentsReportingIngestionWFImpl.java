package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseOrganizationSequentialIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class PaymentsReportingIngestionWFImpl extends BaseOrganizationSequentialIngestionFlowFileWFImpl<PaymentsReportingIngestionFlowFileActivityResult> implements PaymentsReportingIngestionWF {

  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivity;
  private NotifyPaymentsReportingToIufClassificationActivity notifyPaymentsReportingToIufClassificationActivity;

  @Override
  protected Function<Long, PaymentsReportingIngestionFlowFileActivityResult> buildActivityStubs(ApplicationContext applicationContext) {
    PaymentsReportingIngestionWfConfig wfConfig = applicationContext.getBean(PaymentsReportingIngestionWfConfig.class);

    PaymentsReportingIngestionFlowFileActivity paymentsReportingIngestionFlowFileActivity =  wfConfig.buildPaymentsReportingIngestionFlowFileActivityStub();
    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    notifyPaymentsReportingToIufClassificationActivity = wfConfig.buildNotifyPaymentsReportingToIufClassificationActivityStub();

    return paymentsReportingIngestionFlowFileActivity::processFile;
  }

  @Override
  protected IngestionFlowFileProcessingLockerActivity getIngestionFlowFileProcessingLockerActivity() {
    return ingestionFlowFileProcessingLockerActivity;
  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId, PaymentsReportingIngestionFlowFileActivityResult result) {
    notifyPaymentsReportingToIufClassificationActivity
      .signalPaymentsReportingIufClassificationWithStart(
        result.getOrganizationId(),
        result.getIuf(),
        result.getTransfers());
  }
}
