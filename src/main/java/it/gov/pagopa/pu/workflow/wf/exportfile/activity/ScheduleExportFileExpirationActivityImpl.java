package it.gov.pagopa.pu.workflow.wf.exportfile.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.exportfile.wfexportfile.ExportFileWFImpl;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = ExportFileWFImpl.TASK_QUEUE_EXPORT_FILE_LOCAL_ACTIVITY)
public class ScheduleExportFileExpirationActivityImpl implements
    ScheduleExportFileExpirationActivity {

  @Override
  public void scheduleExportFileExpiration(Long exportFileId, LocalDate expirationDate) {
    //TODO scheduling for export file expiration will be added with task P4ADEV-2316
  }
}
