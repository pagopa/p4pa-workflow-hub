package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.IngestionFlowFileProcessingLockerActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.InstallmentIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.debtposition.SynchronizeIngestedDebtPositionActivity;
import it.gov.pagopa.payhub.activities.dto.ingestion.IngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.InstallmentIngestionFlowFileResult;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtposition.SyncIngestedDebtPositionDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseOrganizationSequentialIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.activity.StartMassiveNoticesGenerationWFActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.config.DebtPositionIngestionFlowWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class DebtPositionIngestionFlowWFImpl extends BaseOrganizationSequentialIngestionFlowFileWFImpl<InstallmentIngestionFlowFileResult> implements DebtPositionIngestionFlowWF {

  private IngestionFlowFileProcessingLockerActivity ingestionFlowFileProcessingLockerActivity;
  private SynchronizeIngestedDebtPositionActivity synchronizeIngestedDebtPositionActivity;
  private StartMassiveNoticesGenerationWFActivity startMassiveNoticesGenerationWFActivity;

  @Override
  protected Function<Long, InstallmentIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    DebtPositionIngestionFlowWfConfig wfConfig = applicationContext.getBean(DebtPositionIngestionFlowWfConfig.class);

    InstallmentIngestionFlowFileActivity installmentIngestionFlowFileActivity = wfConfig.buildInstallmentIngestionFlowFileActivityStub();
    ingestionFlowFileProcessingLockerActivity = wfConfig.buildIngestionFlowFileProcessingLockerActivityStub();
    synchronizeIngestedDebtPositionActivity = wfConfig.buildSynchronizeIngestedDebtPositionActivityStub();
    startMassiveNoticesGenerationWFActivity = wfConfig.buildStartMassiveNoticesGenerationWFActivityStub();

    return installmentIngestionFlowFileActivity::processFile;
  }

  @Override
  public void finallyAfterProcessing(Long ingestionFlowFileId, IngestionFlowFileResult ingestionResult) {
    SyncIngestedDebtPositionDTO syncDpResult
      = synchronizeIngestedDebtPositionActivity.synchronizeIngestedDebtPosition(ingestionFlowFileId);

    mergeErrorDescriptions(ingestionResult, "synchronization", syncDpResult.getErrorsDescription());

    if (StringUtils.isNotBlank(syncDpResult.getPdfGeneratedId())) {
      startMassiveNoticesGenerationWFActivity.startMassiveNoticesGenerationWF(ingestionFlowFileId);
    }
  }

  @Override
  protected IngestionFlowFileProcessingLockerActivity getIngestionFlowFileProcessingLockerActivity() {
    return ingestionFlowFileProcessingLockerActivity;
  }
}
