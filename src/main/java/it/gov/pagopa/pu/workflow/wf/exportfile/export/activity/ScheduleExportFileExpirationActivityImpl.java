package it.gov.pagopa.pu.workflow.wf.exportfile.export.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.ExportFileExpirationHandlerWFClient;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@ActivityImpl(taskQueues = ExportFileWFImpl.TASK_QUEUE_EXPORT_FILE_LOCAL_ACTIVITY)
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
