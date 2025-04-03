package it.gov.pagopa.pu.workflow.wf.exportfile.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.time.LocalDate;

@ActivityInterface
public interface ScheduleExportFileExpirationActivity {

  @ActivityMethod
  void scheduleExportFileExpiration(Long exportFileId, LocalDate dueDate);

}
