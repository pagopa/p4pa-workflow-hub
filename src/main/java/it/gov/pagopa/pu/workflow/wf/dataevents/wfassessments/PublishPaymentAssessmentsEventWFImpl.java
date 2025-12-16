package it.gov.pagopa.pu.workflow.wf.dataevents.wfassessments;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
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
public class PublishPaymentAssessmentsEventWFImpl implements PublishPaymentAssessmentsEventWF, ApplicationContextAware {

  private PublishDataEventsActivity publishDataEventsActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DataEventsWFConfig dataEventsWFConfig = applicationContext.getBean(DataEventsWFConfig.class);
    publishDataEventsActivity = dataEventsWFConfig.buildPublishDataEventActivityStub();
  }

  @Override
  public void publishPaymentAssessmentsEvent(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest) {
    log.info("Executing PublishPaymentAssessmentsEventWF");
    publishDataEventsActivity.publishPaymentAssessmentsEventActivity(assessmentsEventDTO, dataEventRequest);
    log.info("PublishPaymentAssessmentsEventWF completed");
  }
}
