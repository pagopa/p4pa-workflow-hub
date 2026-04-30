package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessments.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.assessments.AssessmentsIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.assessments.config.AssessmentsIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class AssessmentsIngestionWFImpl extends BaseIngestionFlowFileWFImpl<AssessmentsIngestionFlowFileResult> implements AssessmentsIngestionWF {

  @Override
  protected Function<Long, AssessmentsIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    AssessmentsIngestionWFConfig wfConfig = applicationContext.getBean(AssessmentsIngestionWFConfig.class);
    return wfConfig.buildAssessmentsIngestionActivityStub()::processFile;
  }

}
