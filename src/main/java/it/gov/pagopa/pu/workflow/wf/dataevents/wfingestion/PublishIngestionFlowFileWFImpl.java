package it.gov.pagopa.pu.workflow.wf.dataevents.wfingestion;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.dataevents.activity.PublishDataEventsActivity;
import it.gov.pagopa.pu.workflow.wf.dataevents.config.DataEventsWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY)
public class PublishIngestionFlowFileWFImpl implements PublishIngestionFlowFileWF, ApplicationContextAware {

  private PublishDataEventsActivity publishDataEventsActivity;

  @Override
  public void publishIngestionFlowFileEvent(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest) {
    log.info("Executing PublishIngestionFlowFileWF");
    publishDataEventsActivity.publishIngestionFlowFileEventActivity(ingestionDataDTO, dataEventRequest);
    log.info("PublishIngestionFlowFileWF completed");
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DataEventsWFConfig dataEventsWFConfig = applicationContext.getBean(DataEventsWFConfig.class);
    publishDataEventsActivity = dataEventsWFConfig.buildPublishDataEventActivityStub();
  }

}
