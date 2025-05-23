package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
@WorkflowImpl(taskQueues = TreasuryOpiIngestionWFImpl.TASK_QUEUE_TREASURY_OPI_INGESTION_WF)
public class TreasuryOpiIngestionWFImpl extends BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> implements TreasuryOpiIngestionWF {
  public static final String TASK_QUEUE_TREASURY_OPI_INGESTION_WF = "TreasuryOpiIngestionWF";
  public static final String TASK_QUEUE_TREASURY_OPI_INGESTION_LOCAL_ACTIVITY = "TreasuryOpiIngestionWF_LOCAL";

  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivity;


  @Override
  protected TreasuryOpiIngestionActivity buildActivityStubs(ApplicationContext applicationContext) {
    TreasuryOpiIngestionWfConfig wfConfig = applicationContext.getBean(TreasuryOpiIngestionWfConfig.class);

    TreasuryOpiIngestionActivity treasuryOpiIngestionActivity = wfConfig.buildTreasuryOpiIngestionActivityStub();
    notifyTreasuryToIufClassificationActivity = wfConfig.buildNotifyTreasuryToIufClassificationActivityStub();

    return treasuryOpiIngestionActivity;
  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId, TreasuryIufIngestionFlowFileResult result) {
    result.getIuf2TreasuryIdMap().forEach((iuf, treasuryId) ->
      notifyTreasuryToIufClassificationActivity.signalIufClassificationWithStart(
        result.getOrganizationId(),
        iuf,
        treasuryId
      ));
  }
}
