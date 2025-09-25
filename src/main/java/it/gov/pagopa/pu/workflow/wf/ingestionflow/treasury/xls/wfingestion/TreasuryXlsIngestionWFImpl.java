package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.config.TreasuryXlsIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class TreasuryXlsIngestionWFImpl extends BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> implements TreasuryXlsIngestionWF {
  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivity;

  @Override
  protected Function<Long, TreasuryIufIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    TreasuryXlsIngestionWFConfig wfConfig = applicationContext.getBean(TreasuryXlsIngestionWFConfig.class);
    notifyTreasuryToIufClassificationActivity = wfConfig.buildNotifyTreasuryToIufClassificationActivityStub();
    return wfConfig.buildTreasuryXlsIngestionActivityStub()::processFile;
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
