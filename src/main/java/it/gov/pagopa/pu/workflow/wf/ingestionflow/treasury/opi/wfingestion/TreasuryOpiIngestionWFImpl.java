package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.treasury.TreasuryIufResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class TreasuryOpiIngestionWFImpl implements TreasuryOpiIngestionWF, ApplicationContextAware {
  public static final String TASK_QUEUE = "TreasuryOpiIngestionWF";

  private TreasuryOpiIngestionActivity treasuryOpiIngestionActivity;
  private UpdateIngestionFlowStatusActivity updateIngestionFlowStatusActivity;
  private SendEmailIngestionFlowActivity sendEmailIngestionFlowActivity;
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    TreasuryOpiIngestionWfConfig wfConfig = applicationContext.getBean(TreasuryOpiIngestionWfConfig.class);

    treasuryOpiIngestionActivity = wfConfig.buildTreasuryOpiIngestionActivityStub();
    updateIngestionFlowStatusActivity = wfConfig.buildUpdateIngestionFlowStatusActivityStub();
    sendEmailIngestionFlowActivity = wfConfig.buildSendEmailIngestionFlowActivityStub();
    notifyTreasuryToIufClassificationActivity = wfConfig.buildNotifyTreasuryToIufClassificationActivityStub();
  }

  @Override
  public void ingest(Long ingestionFlowFileId) {
    log.info("Handling ingestionFlowFileId {}", ingestionFlowFileId);

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    TreasuryIufResult ingestionResult = treasuryOpiIngestionActivity.processFile(ingestionFlowFileId);
    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, ingestionResult.isSuccess());
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      ingestionResult.isSuccess()
        ? IngestionFlowFile.StatusEnum.COMPLETED
        : IngestionFlowFile.StatusEnum.ERROR,
      ingestionResult.getErrorDescription(),
      ingestionResult.getDiscardedFileName());

    ingestionResult.getIufTreasuryIdMap().forEach((iuf, treasuryId) ->
      notifyTreasuryToIufClassificationActivity.signalIufClassificationWithStart(
        ingestionResult.getOrganizationId(),
        iuf,
        treasuryId
      ));
    log.info("Ingestion with ID {} is completed", ingestionFlowFileId);
  }
}
