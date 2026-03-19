package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile.DeleteMassiveNoticesFileWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
  public void scheduleMassiveNoticesFileDeletionWF(Long ingestionFlowFileId, LocalDate scheduleDate) {
    log.info("Start of scheduling the delete massive notices file WF: {}, on {}", ingestionFlowFileId, scheduleDate);
    String workflowId = generateWorkflowId(ingestionFlowFileId, DeleteMassiveNoticesFileWF.class);
    DeleteMassiveNoticesFileWF workflow = workflowService.buildWorkflowStubScheduled(
      DeleteMassiveNoticesFileWF.class,
      TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY,
      workflowId,
      scheduleDate
    );
    workflowClientService.start(workflow::deleteMassiveNoticesFile, ingestionFlowFileId);
  }
}
