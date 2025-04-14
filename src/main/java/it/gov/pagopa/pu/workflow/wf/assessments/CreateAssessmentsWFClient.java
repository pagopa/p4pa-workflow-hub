package it.gov.pagopa.pu.workflow.wf.assessments;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsWF;
import it.gov.pagopa.pu.workflow.wf.assessments.wfassessments.CreateAssessmentsWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class CreateAssessmentsWFClient {

  private final WorkflowService workflowService;

  public CreateAssessmentsWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String createAssessments(Long receiptId) {
    log.info("Starting create assessments for receipt with id: {}", receiptId);

    String taskQueue = CreateAssessmentsWFImpl.TASK_QUEUE_CREATE_ASSESSMENTS_WF;
    String workflowId  = generateWorkflowId(receiptId, CreateAssessmentsWF.class);

    CreateAssessmentsWF workflow = workflowService.buildWorkflowStub(
      CreateAssessmentsWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::createAssessment, receiptId);
    return workflowId;
  }
}
