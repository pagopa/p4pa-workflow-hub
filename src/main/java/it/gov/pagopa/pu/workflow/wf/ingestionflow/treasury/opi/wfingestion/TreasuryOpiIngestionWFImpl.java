package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.email.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.activity.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;

@Slf4j
@WorkflowImpl(taskQueues = TreasuryOpiIngestionWFImpl.TASK_QUEUE_TREASURY_OPI_INGESTION_WF)
public class TreasuryOpiIngestionWFImpl implements TreasuryOpiIngestionWF, ApplicationContextAware {
  public static final String TASK_QUEUE_TREASURY_OPI_INGESTION_WF = "TreasuryOpiIngestionWF";

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
    log.info("Handling Treasury OPI ingestionFlowFileId {}", ingestionFlowFileId);
    TreasuryIufIngestionFlowFileResult ingestionResult;

    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId, IngestionFlowFile.StatusEnum.PROCESSING, null, null);
    ingestionResult = processFile(ingestionFlowFileId);
    boolean success = StringUtils.isEmpty(ingestionResult.getErrorDescription());

    sendEmailIngestionFlowActivity.sendEmail(ingestionFlowFileId, success);
    updateIngestionFlowStatusActivity.updateStatus(ingestionFlowFileId,
      success
        ? IngestionFlowFile.StatusEnum.COMPLETED
        : IngestionFlowFile.StatusEnum.ERROR,
      ingestionResult.getErrorDescription(),
      ingestionResult.getDiscardedFileName());

    log.info("Treasury OPI ingestion with ID {} is completed", ingestionFlowFileId);
  }

  private TreasuryIufIngestionFlowFileResult processFile(Long ingestionFlowFileId) {
    TreasuryIufIngestionFlowFileResult ingestionResult;
    try{
      ingestionResult = treasuryOpiIngestionActivity.processFile(ingestionFlowFileId);
      Long organizationId = ingestionResult.getOrganizationId();

      ingestionResult.getIuf2TreasuryIdMap().forEach((iuf, treasuryId) ->
        notifyTreasuryToIufClassificationActivity.signalIufClassificationWithStart(
          organizationId,
          iuf,
          treasuryId
        ));
    } catch (Exception e){
      ingestionResult = new TreasuryIufIngestionFlowFileResult(
        Collections.emptyMap(),
        null,
        "Unexpected error when processing TreasuryOPI file: " + e.getMessage(),
        null
      );
    }
    return ingestionResult;
  }
}
