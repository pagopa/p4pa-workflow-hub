package it.gov.pagopa.pu.workflow.wf.dataevents.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.assessments.AssessmentEventDTO;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.producer.DataEventsProducerService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY)
public class PublishDataEventsActivityImpl implements PublishDataEventsActivity {
  private final DataEventsProducerService dataEventsProducerService;

  public PublishDataEventsActivityImpl(DataEventsProducerService dataEventsProducerService) {
    this.dataEventsProducerService = dataEventsProducerService;
  }

  @Override
  public void publishExportFileEventActivity(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequestDTO) {
    log.info("Publishing ExportFile event having exportFileId {}", exportDataDTO.getExportFileId());
    dataEventsProducerService.notifyExportEvent(exportDataDTO, dataEventRequestDTO);
  }

  @Override
  public void publishIngestionFlowFileEventActivity(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest) {
    log.info("Publishing IngestionFlowFile event having ingestionFlowFileId {}", ingestionDataDTO.getIngestionFlowFileId());
    dataEventsProducerService.notifyIngestionEvent(ingestionDataDTO, dataEventRequest);
  }

  @Override
  public void publishPaymentAssessmentsEventActivity(AssessmentEventDTO assessmentsEventDTO, DataEventRequestDTO dataEventRequest) {
    log.info("Publishing PaymentAssessments event having assessmentId {}", assessmentsEventDTO.getAssessmentId());
    dataEventsProducerService.notifyPaymentAssessmentsEvent(assessmentsEventDTO, dataEventRequest);
  }

}
