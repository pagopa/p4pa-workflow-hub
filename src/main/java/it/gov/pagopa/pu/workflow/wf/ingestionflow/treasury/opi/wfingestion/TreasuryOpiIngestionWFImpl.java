package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.treasury.TreasuryOpiIngestionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.treasury.TreasuryIufIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity.NotifyTreasuryToIufClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.config.TreasuryOpiIngestionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class TreasuryOpiIngestionWFImpl extends BaseIngestionFlowFileWFImpl<TreasuryIufIngestionFlowFileResult> implements TreasuryOpiIngestionWF {

  private NotifyTreasuryToIufClassificationActivity notifyTreasuryToIufClassificationActivity;


  @Override
  protected Function<Long, TreasuryIufIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    TreasuryOpiIngestionWfConfig wfConfig = applicationContext.getBean(TreasuryOpiIngestionWfConfig.class);

    TreasuryOpiIngestionActivity treasuryOpiIngestionActivity = wfConfig.buildTreasuryOpiIngestionActivityStub();
    notifyTreasuryToIufClassificationActivity = wfConfig.buildNotifyTreasuryToIufClassificationActivityStub();

    return treasuryOpiIngestionActivity::processFile;
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
