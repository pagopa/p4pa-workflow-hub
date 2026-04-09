package it.gov.pagopa.pu.workflow.service.temporal;

import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.exception.custom.TooManyAttemptsException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowConflictException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Set;

import static io.temporal.api.enums.v1.WorkflowExecutionStatus.*;


@Lazy
@Slf4j
@Service
public class WorkflowCompletionService {

  private static final String ERR_WF_EXISTS = "[WF_ALREADY_EXISTS] - %s already exists and not terminated";

  private final WorkflowService workflowService;

    /**
     * <a href="https://docs.temporal.io/workflows#status">Closed statuses</a>
     */
    private final Set<WorkflowExecutionStatus> wfTerminationStatuses = Set.of(
            WORKFLOW_EXECUTION_STATUS_FAILED,
            WORKFLOW_EXECUTION_STATUS_TERMINATED,
            WORKFLOW_EXECUTION_STATUS_CANCELED,
            WORKFLOW_EXECUTION_STATUS_TIMED_OUT,
            WORKFLOW_EXECUTION_STATUS_COMPLETED
    );

    public WorkflowCompletionService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Waits for a workflow to reach a terminal status.
     *
     * @param workflowId   The ID of the workflow to monitor.
     * @param maxAttempts  The maximum number of retry attempts.
     * @param retryDelayMs The delay in milliseconds between retries.
     * @return The final {@link WorkflowStatusDTO}.
     * @throws TooManyAttemptsException If the retry limit is exceeded.
     */
    public WorkflowStatusDTO waitTerminationStatus(String workflowId, int maxAttempts, int retryDelayMs) {

        maxAttempts = Math.max(maxAttempts, 1);
        int attempts = 0;

        do {
          WorkflowStatusDTO workflowStatus = workflowService.getWorkflowStatus(workflowId);
          log.debug("Retrieved workflow status: {}", workflowStatus);

            if (workflowStatus.getStatus() != null && wfTerminationStatuses.contains(workflowStatus.getStatus())) {
                return workflowStatus;
            }

            attempts++;

            try {
                Thread.sleep(retryDelayMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while waiting for workflow completion. Attempt {}/{}", attempts, maxAttempts);
            }
        } while (attempts <= maxAttempts);

        log.info("Workflow {} did not complete after {} retries. No further attempts will be made.", workflowId, maxAttempts);
        throw new TooManyAttemptsException("[TOO_MANY_ATTEMPTS] Maximum number of retries reached for workflow " + workflowId);
    }


  /**
   * Check Workflow exists and not terminated.
   *
   * @param workflowId The ID of the workflow to check.
   * @throws WorkflowConflictException If workflow exists and not in a terminal status.
   */
  public void checkWorkflowExistsAndNotTerminated(String workflowId) {
    WorkflowStatusDTO wfStatus = null;

    try {
      wfStatus = workflowService.getWorkflowStatus(workflowId);
    } catch (WorkflowNotFoundException e) {
      log.info("Workflow with ID {} not found", workflowId);
    }

    if (wfStatus != null && !wfTerminationStatuses.contains(wfStatus.getStatus())) {
      log.warn("Conflict detected: workflow {} already exists in status {}", workflowId, wfStatus.getStatus());
      throw new WorkflowConflictException(String.format(ERR_WF_EXISTS, workflowId));
    }
  }
}
