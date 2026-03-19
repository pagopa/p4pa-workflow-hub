package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile.DeleteMassiveNoticesFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL)
public class ScheduleMassiveNoticesFileDeletionWFActivityImpl implements ScheduleMassiveNoticesFileDeletionWFActivity {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public ScheduleMassiveNoticesFileDeletionWFActivityImpl(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  @Override
  public void scheduleMassiveNoticesFileDeletionWF(Long ingestionFlowFileId, Duration retentionDuration) {
    log.info("Start of scheduling the delete massive notices file WF: {}, with delay of {} days", ingestionFlowFileId, retentionDuration.toDays());
    String workflowId = generateWorkflowId(ingestionFlowFileId, DeleteMassiveNoticesFileWF.class);
    DeleteMassiveNoticesFileWF workflow = workflowService.buildWorkflowStubDelayed(
      DeleteMassiveNoticesFileWF.class,
      TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY,
      workflowId,
      retentionDuration
    );
    workflowClientService.start(workflow::deleteMassiveNoticesFile, ingestionFlowFileId);
  }
}
