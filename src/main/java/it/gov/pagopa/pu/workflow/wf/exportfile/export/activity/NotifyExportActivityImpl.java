package it.gov.pagopa.pu.workflow.wf.exportfile.export.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.producer.DataEventsProducerService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_EXPORT_MEDIUM_PRIORITY_LOCAL)
public class NotifyExportActivityImpl implements NotifyExportActivity {
  private final DataEventsProducerService dataEventsProducerService;

  public NotifyExportActivityImpl(DataEventsProducerService dataEventsProducerService) {
    this.dataEventsProducerService = dataEventsProducerService;
  }

  @Override
  public void notifyExportEvent(ExportDataDTO exportDataDTO, DataEventRequestDTO dataEventRequest) {
    log.info("notifyExportEvent - exportFileId: {}", exportDataDTO.getExportFileId());
    dataEventsProducerService.notifyExportEvent(exportDataDTO, dataEventRequest);
  }
}
