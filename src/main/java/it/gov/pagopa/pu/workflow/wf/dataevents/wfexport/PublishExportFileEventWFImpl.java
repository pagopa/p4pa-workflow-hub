package it.gov.pagopa.pu.workflow.wf.dataevents.wfexport;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
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
public class PublishExportFileEventWFImpl implements PublishExportFileEventWF, ApplicationContextAware {

  private PublishDataEventsActivity publishDataEventsActivity;

  @Override
  public void publishExportFileEvent(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequestDTO) {
    log.info("Executing PublishExportFileEventWF");
    publishDataEventsActivity.publishExportFileEventActivity(exportDataDTO, dataEventRequestDTO);
    log.info("PublishExportFileEventWF completed");
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DataEventsWFConfig dataEventsWFConfig = applicationContext.getBean(DataEventsWFConfig.class);
    publishDataEventsActivity = dataEventsWFConfig.buildPublishDataEventActivityStub();
  }
}
