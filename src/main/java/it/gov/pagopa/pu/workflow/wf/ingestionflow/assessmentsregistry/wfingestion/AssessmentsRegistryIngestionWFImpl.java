package it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.assessmentsregistry.AssessmentsRegistryIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.assessmentsregistry.config.AssessmentsRegistryIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class AssessmentsRegistryIngestionWFImpl extends BaseIngestionFlowFileWFImpl<AssessmentsRegistryIngestionFlowFileResult> implements AssessmentsRegistryIngestionWF {

  @Override
  protected Function<Long, AssessmentsRegistryIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    AssessmentsRegistryIngestionWFConfig wfConfig = applicationContext.getBean(AssessmentsRegistryIngestionWFConfig.class);
    return wfConfig.buildAssessmentsRegistryIngestionActivityStub()::processFile;
  }

}
