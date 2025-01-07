package it.gov.pagopa.pu.workflow.service;

import com.uber.m3.tally.NoopScope;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.TemporalException;
import io.temporal.internal.client.WorkflowClientHelper;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

  private final WorkflowClient workflowClient;

  public WorkflowServiceImpl(WorkflowClient workflowClient) {
    this.workflowClient = workflowClient;
  }

  @Override
  public <T> T buildWorkflowStub(Class<T> workflowClass, String taskQueue, String workflowId) {
    return workflowClient.newWorkflowStub(
      workflowClass,
      WorkflowOptions.newBuilder()
        .setTaskQueue(taskQueue)
        .setWorkflowId(workflowId)
        .build());
  }

  @Override
  public WorkflowStatusDTO getWorkflowStatus(String workflowId) {
    try {
      log.debug("Retrieving workflow status for workflowId: {}", workflowId);
      WorkflowExecutionInfo info = WorkflowClientHelper.describeWorkflowInstance(
        workflowClient.getWorkflowServiceStubs(),
        "default",
        WorkflowExecution.newBuilder().setWorkflowId(workflowId).build(),
        new NoopScope()
      );

      return WorkflowStatusDTO.builder()
        .workflowId(workflowId)
        .status(info.getStatus().name())
        .build();

    } catch (io.temporal.client.WorkflowNotFoundException e) {
      log.error("Workflow with ID {} not found", workflowId);
      throw new WorkflowNotFoundException(e.getMessage());

    } catch (TemporalException e) {
      log.error("An error occurred while retrieving the workflow status: {}", e.getMessage());
      throw new WorkflowInternalErrorException(e.getMessage());
    }
  }
}
