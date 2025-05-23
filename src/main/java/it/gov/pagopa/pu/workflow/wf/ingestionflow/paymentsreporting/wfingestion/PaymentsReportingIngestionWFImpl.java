package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity.NotifyPaymentsReportingToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.config.PaymentsReportingIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_WF;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_WF)
public class PaymentsReportingIngestionWFImpl extends BaseIngestionFlowFileWFImpl<PaymentsReportingIngestionFlowFileActivityResult> implements PaymentsReportingIngestionWF {
  public static final String TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_WF = "PaymentsReportingIngestionWF";
  public static final String TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_LOCAL_ACTIVITY = "PaymentsReportingIngestionWF_LOCAL";

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
    notifyPaymentsReportingToIufClassificationActivity.signalIufClassificationWithStart(
      result.getOrganizationId(),
      result.getIuf(),
      result.getTransfers());
  }
}
