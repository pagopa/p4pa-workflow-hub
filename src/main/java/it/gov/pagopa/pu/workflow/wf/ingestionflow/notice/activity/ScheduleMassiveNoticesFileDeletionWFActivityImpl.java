package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
  public void scheduleFileDeletion(Long ingestionFlowFileId, LocalDate scheduleDate) {
    log.info("Start of scheduling the massive notices file deletion WF: {}, on {}", ingestionFlowFileId, scheduleDate);
//    String workflowId = generateWorkflowId(ingestionFlowFileId, PH);
//    PH workflow = workflowService.buildWorkflowStubScheduled(
//      PH,
//      TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY,
//      workflowId,
//      scheduleDate
//    );
//    workflowClientService.start(workflow::PH, ingestionFlowFileId);
  }
}
