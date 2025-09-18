package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csv.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.csv.config.TreasuryCsvIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class TreasuryCsvIngestionWFImpl extends BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> implements TreasuryCsvIngestionWF {
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivity;

  @Override
  protected Function<Long, TreasuryIufIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    TreasuryCsvIngestionWFConfig wfConfig = applicationContext.getBean(TreasuryCsvIngestionWFConfig.class);
    notifyTreasuryToIufClassificationActivity = wfConfig.buildNotifyTreasuryToIufClassificationActivityStub();
    return wfConfig.buildTreasuryCsvIngestionActivityStub()::processFile;
  }

  @Override
  protected void afterProcessing(Long ingestionFlowFileId, TreasuryIufIngestionFlowFileResult result) {
    result.getIuf2TreasuryIdMap().forEach((iuf, treasuryId) ->
      notifyTreasuryToIufClassificationActivity.signalTreasuryIufClassificationWithStart(
        result.getOrganizationId(),
        iuf,
        treasuryId
      ));
  }
}
