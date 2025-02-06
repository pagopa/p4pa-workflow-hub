package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.HandleDebtPositionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.HandleDebtPositionExpirationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class HandleDebtPositionExpirationWfClientImpl implements HandleDebtPositionExpirationWfClient {

  private final WorkflowService workflowService;

  public HandleDebtPositionExpirationWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String handleDpExpiration(Long debtPositionId) {
    log.info("Starting debt position expiration WF: {}", debtPositionId);
    String workflowId = generateWorkflowId(debtPositionId, HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF);
    HandleDebtPositionExpirationWF workflow = workflowService.buildWorkflowStub(
      HandleDebtPositionExpirationWF.class,
      HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF,
      workflowId);
    OffsetDateTime nextDueDate = workflow.handleDpExpiration(debtPositionId);
    WorkflowClient.start(workflow::handleDpExpiration, debtPositionId);

    log.info("Start of scheduling the next debt position expiration WF: {}, on {}", debtPositionId, nextDueDate.plusDays(1));
    HandleDebtPositionExpirationWF scheduledWorkflow = workflowService.buildWorkflowStubScheduled(
      HandleDebtPositionExpirationWF.class,
      HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF,
      workflowId,
      Duration.between(nextDueDate.plusDays(1), OffsetDateTime.now())
    );
    WorkflowClient.start(scheduledWorkflow::handleDpExpiration, debtPositionId);
    return workflowId;
  }
}
