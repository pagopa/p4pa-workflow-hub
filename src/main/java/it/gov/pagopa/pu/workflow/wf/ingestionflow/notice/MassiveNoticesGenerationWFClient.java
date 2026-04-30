package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration.MassiveNoticesGenerationWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class MassiveNoticesGenerationWFClient {
  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public MassiveNoticesGenerationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO generate(Long ingestionFlowFileId) {
    log.info("Starting on-demand massive notices generation wf for ingestionFlowFileId {}", ingestionFlowFileId);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    String workflowId = generateWorkflowId(ingestionFlowFileId, MassiveNoticesGenerationWF.class);

    MassiveNoticesGenerationWF workflow = workflowService.buildWorkflowStubToStartNew(
      MassiveNoticesGenerationWF.class,
      taskQueue,
      workflowId
    );

    return workflowClientService.start(workflow::generate, ingestionFlowFileId);
  }
}
