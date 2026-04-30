package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtpositiontypeorg.DebtPositionTypeOrgIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontypeorg.config.DebtPositionTypeOrgIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class DebtPositionTypeOrgIngestionWFImpl extends BaseIngestionFlowFileWFImpl<DebtPositionTypeOrgIngestionFlowFileResult> implements DebtPositionTypeOrgIngestionWF {

    @Override
    protected Function<Long, DebtPositionTypeOrgIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
        DebtPositionTypeOrgIngestionWFConfig wfConfig = applicationContext.getBean(DebtPositionTypeOrgIngestionWFConfig.class);
        return wfConfig.buildDebtPositionTypeOrgIngestionActivityStub()::processFile;
    }

}
