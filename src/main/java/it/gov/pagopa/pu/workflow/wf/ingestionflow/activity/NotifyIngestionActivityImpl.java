package it.gov.pagopa.pu.workflow.wf.ingestionflow.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.dto.IngestionDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.producer.DataEventsProducerService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL)
public class NotifyIngestionActivityImpl implements NotifyIngestionActivity {
  private final DataEventsProducerService dataEventsProducerService;

  public NotifyIngestionActivityImpl(DataEventsProducerService dataEventsProducerService) {
    this.dataEventsProducerService = dataEventsProducerService;
  }

  @Override
  public void notifyIngestionEvent(IngestionDataDTO ingestionDataDTO, DataEventRequestDTO dataEventRequest) {
    log.info("notifyIngestionEvent - ingestionFlowFileId: {}", ingestionDataDTO.getIngestionFlowFileId());
    dataEventsProducerService.notifyIngestionEvent(ingestionDataDTO, dataEventRequest);
  }
}
