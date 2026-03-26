package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration.MassiveNoticesGenerationWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL)
public class StartMassiveNoticesGenerationWFActivityImpl implements StartMassiveNoticesGenerationWFActivity {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public StartMassiveNoticesGenerationWFActivityImpl(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  @Override
  public void startMassiveNoticesGenerationWF(Long ingestionFlowFileId) {
    log.info("Starting massive notices generation WF: {}", ingestionFlowFileId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ingestionFlowFileId, MassiveNoticesGenerationWF.class);
    MassiveNoticesGenerationWF workflow = workflowService.buildWorkflowStubToStartNew(
      MassiveNoticesGenerationWF.class,
      taskQueue,
      workflowId);
    workflowClientService.start(workflow::generate, ingestionFlowFileId);
  }
}
