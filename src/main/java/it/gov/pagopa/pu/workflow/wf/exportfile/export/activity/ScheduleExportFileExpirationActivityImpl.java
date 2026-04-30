package it.gov.pagopa.pu.workflow.wf.exportfile.export.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.ExportFileExpirationHandlerWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_EXPORT_MEDIUM_PRIORITY_LOCAL)
public class ScheduleExportFileExpirationActivityImpl implements ScheduleExportFileExpirationActivity {

  private final ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClient;

  public ScheduleExportFileExpirationActivityImpl(ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClient) {
    this.exportFileExpirationHandlerWFClient = exportFileExpirationHandlerWFClient;
  }

  @Override
  public void scheduleExportFileExpiration(Long exportFileId, LocalDate expirationDate) {
    exportFileExpirationHandlerWFClient.scheduleExportFileExpiration(exportFileId, expirationDate);
  }
}
