package it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.organization.OrganizationIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.organization.config.OrganizationIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class OrganizationIngestionWFImpl extends BaseIngestionFlowFileWFImpl<OrganizationIngestionFlowFileResult> implements OrganizationIngestionWF {

  @Override
  protected Function<Long, OrganizationIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    OrganizationIngestionWFConfig wfConfig = applicationContext.getBean(OrganizationIngestionWFConfig.class);
    return wfConfig.buildOrganizationIngestionActivityStub()::processFile;
  }

}
