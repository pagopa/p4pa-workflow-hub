package it.gov.pagopa.pu.workflow.wf.exportfile.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWFImpl;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DP_EXPIRATION_LOCAL_ACTIVITY)
public class ScheduleExportFileExpirationActivityImpl implements
    ScheduleExportFileExpirationActivity {

  @Override
  public void scheduleExportFileExpiration(Long exportFileId, LocalDate dueDate) {
    //TODO scheduling for export file expiration will be added with task P4ADEV-2316
  }
}
