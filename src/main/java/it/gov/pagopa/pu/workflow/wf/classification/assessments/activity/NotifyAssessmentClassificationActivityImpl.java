package it.gov.pagopa.pu.workflow.wf.classification.assessments.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.producer.DataEventsProducerService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL)
public class NotifyAssessmentClassificationActivityImpl implements NotifyAssessmentClassificationActivity {
  private final DataEventsProducerService dataEventsProducerService;

  public NotifyAssessmentClassificationActivityImpl(DataEventsProducerService dataEventsProducerService) {
    this.dataEventsProducerService = dataEventsProducerService;
  }

  @Override
  public void notifyAssessmentClassificationEvent(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest) {
    log.info("notifyAssessmentClassificationEvent - organizationId: {}, iuv: {}, iud: {}", assessmentsEventDTO.getOrganizationId(), assessmentsEventDTO.getIuv(), assessmentsEventDTO.getIud());
    dataEventsProducerService.notifyPaymentAssessmentsEvent(assessmentsEventDTO, dataEventRequest);
  }
}
