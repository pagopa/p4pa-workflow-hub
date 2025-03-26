package it.gov.pagopa.pu.workflow.wf.assessments;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.wfassessments.CreateAssessmentsWF;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.wfassessments.CreateAssessmentsWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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
    String workflowId  = generateWorkflowId(receiptId, taskQueue);

    CreateAssessmentsWF workflow = workflowService.buildWorkflowStub(
      CreateAssessmentsWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::create, receiptId);
    return workflowId;
  }

  /**
   * Cannot invoke a WF from WF thread, using Async to use an external thread instead */
  @Async
  public Future<String> createAssessmentsAsyncStart(Long receiptId) {
    return CompletableFuture.completedFuture(createAssessments(receiptId));
  }

}
