package it.gov.pagopa.pu.workflow.wf.ingestionflow.send.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.ingestion.sendnotification.SendNotificationIngestionFlowFileResult;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.BaseIngestionFlowFileWFImpl;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.send.config.SendNotificationIngestionWFConfig;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY)
public class SendNotificationIngestionFlowWFImpl extends BaseIngestionFlowFileWFImpl<SendNotificationIngestionFlowFileResult>
  implements SendNotificationIngestionFlowWF{

  @Override
  protected Function<Long, SendNotificationIngestionFlowFileResult> buildActivityStubs(
    ApplicationContext applicationContext) {
    SendNotificationIngestionWFConfig wfConfig = applicationContext.getBean(SendNotificationIngestionWFConfig.class);
    return wfConfig.buildAssessmentsIngestionActivityStub()::processFile;
  }

}
