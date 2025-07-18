package it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.orgsilservice.OrgSilServiceIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.orgsilservice.config.OrgSilServiceIngestionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.function.Function;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class OrgSilServiceIngestionWFImpl extends BaseIngestionFlowFileWFImpl<OrgSilServiceIngestionFlowFileResult> implements OrgSilServiceIngestionWF {

  @Override
  protected Function<Long, OrgSilServiceIngestionFlowFileResult> buildActivityStubs(ApplicationContext applicationContext) {
    OrgSilServiceIngestionWFConfig wfConfig = applicationContext.getBean(OrgSilServiceIngestionWFConfig.class);
    return wfConfig.buildOrgSilServiceIngestionActivityStub()::processFile;
  }

}
