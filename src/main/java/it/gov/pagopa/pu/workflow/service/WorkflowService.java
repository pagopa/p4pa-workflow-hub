package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

  private final WorkflowClient workflowClient;

  public WorkflowService(WorkflowClient workflowClient) {
    this.workflowClient = workflowClient;
  }

  public <T> T buildWorkflowStub(Class<T> workflowClass, String taskQueue, String workflowId){
    return workflowClient.newWorkflowStub(
      workflowClass,
      WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setWorkflowId(workflowId)
        .build());
  }
}
