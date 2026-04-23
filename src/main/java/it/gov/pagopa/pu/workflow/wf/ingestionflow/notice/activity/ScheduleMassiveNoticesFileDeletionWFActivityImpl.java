package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.DeleteMassiveNoticesFileWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL)
public class ScheduleMassiveNoticesFileDeletionWFActivityImpl implements ScheduleMassiveNoticesFileDeletionWFActivity {
  private final DeleteMassiveNoticesFileWFClient deleteMassiveNoticesFileWFClient;
  private final Duration retentionDuration;

  public ScheduleMassiveNoticesFileDeletionWFActivityImpl(
    DeleteMassiveNoticesFileWFClient deleteMassiveNoticesFileWFClient,
    @Value("${workflow.massive-notices-generation.retention-days}") int retentionDays
  ) {
    this.deleteMassiveNoticesFileWFClient = deleteMassiveNoticesFileWFClient;
    this.retentionDuration = Duration.ofDays(retentionDays);
  }

  @Override
  public void scheduleMassiveNoticesFileDeletionWF(Long ingestionFlowFileId) {
    deleteMassiveNoticesFileWFClient.scheduleMassiveNoticesFileDeletion(ingestionFlowFileId, retentionDuration);
  }
}
