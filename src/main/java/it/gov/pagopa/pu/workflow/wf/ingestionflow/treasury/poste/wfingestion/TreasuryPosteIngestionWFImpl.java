package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.config.TreasuryPosteIngestionWFConfig;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class TreasuryPosteIngestionWFImpl extends
  BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> implements
  TreasuryPosteIngestionWF {

  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivity;

  @Override
  protected Function<Long, TreasuryIufIngestionFlowFileResult> buildActivityStubs(
    ApplicationContext applicationContext) {

    TreasuryPosteIngestionWFConfig wfConfig = applicationContext.getBean(
      TreasuryPosteIngestionWFConfig.class);
    notifyTreasuryToIufClassificationActivity = wfConfig.buildNotifyTreasuryToIufClassificationActivityStub();
    return wfConfig.buildTreasuryPosteIngestionActivityStub()::processFile;

  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId,
    TreasuryIufIngestionFlowFileResult result) {
    result.getIuf2TreasuryIdMap().forEach((iuf, treasuryId) ->
      notifyTreasuryToIufClassificationActivity.signalTreasuryIufClassificationWithStart(
        result.getOrganizationId(),
        iuf,
        treasuryId
      ));
  }

}
