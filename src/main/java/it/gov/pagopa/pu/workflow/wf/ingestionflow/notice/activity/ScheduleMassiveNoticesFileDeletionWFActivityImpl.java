package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile.DeleteMassiveNoticesFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL)
public class ScheduleMassiveNoticesFileDeletionWFActivityImpl implements ScheduleMassiveNoticesFileDeletionWFActivity {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private final Duration retentionDuration;

  public ScheduleMassiveNoticesFileDeletionWFActivityImpl(
    WorkflowService workflowService,
    WorkflowClientService workflowClientService,
    @Value("${workflow.massive-notices-generation.retention-days}") int retentionDays
  ) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
    this.retentionDuration = Duration.ofDays(retentionDays);
  }

  @Override
  public void scheduleMassiveNoticesFileDeletionWF(Long ingestionFlowFileId) {
    log.info("Start of scheduling the delete massive notices file WF: {}, with delay of {} days", ingestionFlowFileId, this.retentionDuration.toDays());
    String workflowId = generateWorkflowId(ingestionFlowFileId, DeleteMassiveNoticesFileWF.class);
    DeleteMassiveNoticesFileWF workflow = workflowService.buildWorkflowStubDelayed(
      DeleteMassiveNoticesFileWF.class,
      TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY,
      workflowId,
      this.retentionDuration
    );
    workflowClientService.start(workflow::deleteMassiveNoticesFile, ingestionFlowFileId);
  }
}
