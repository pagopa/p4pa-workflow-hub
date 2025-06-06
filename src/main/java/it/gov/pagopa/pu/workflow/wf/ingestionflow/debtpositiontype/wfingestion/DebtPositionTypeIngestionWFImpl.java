package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.debtpositiontype.DebtPositionTypeIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.config.DebtPositionTypeIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = DebtPositionTypeIngestionWFImpl.TASK_QUEUE_DEBT_POSITION_TYPE_INGESTION_WF)
public class DebtPositionTypeIngestionWFImpl extends BaseIngestionFlowFileWFImpl<DebtPositionTypeIngestionFlowFileResult> implements DebtPositionTypeIngestionWF {
    public static final String TASK_QUEUE_DEBT_POSITION_TYPE_INGESTION_WF = "DebtPositionTypeIngestionWF";

    @Override
    protected Function<Long, DebtPositionTypeIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
        DebtPositionTypeIngestionWFConfig wfConfig = applicationContext.getBean(DebtPositionTypeIngestionWFConfig.class);
        return wfConfig.buildDebtPositionTypeIngestionActivityStub()::processFile;
    }

}
