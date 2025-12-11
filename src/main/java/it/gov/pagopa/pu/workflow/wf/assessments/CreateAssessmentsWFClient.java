package it.gov.pagopa.pu.workflow.wf.assessments;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class CreateAssessmentsWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public CreateAssessmentsWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO createAssessments(Long receiptId) {
    log.info("Starting create assessments for receipt with id: {}", receiptId);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_ASSESSMENTS_RESERVED_CREATION;
    String workflowId  = generateWorkflowId(receiptId, CreateAssessmentsWF.class);

    CreateAssessmentsWF workflow = workflowService.buildWorkflowStubToStartNew(
      CreateAssessmentsWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::createAssessment, receiptId);
  }
}
