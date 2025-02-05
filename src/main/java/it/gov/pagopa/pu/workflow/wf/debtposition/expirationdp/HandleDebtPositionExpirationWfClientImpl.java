package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
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
  public String handleDpExpiration(DebtPositionDTO debtPosition) {
    log.info("Starting debt position expiration WF: {}", debtPosition.getDebtPositionId());
    String workflowId = generateWorkflowId(debtPosition.getDebtPositionId(), HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF);
    HandleDebtPositionExpirationWF workflow = workflowService.buildWorkflowStub(
      HandleDebtPositionExpirationWF.class,
      HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF,
      workflowId);
    OffsetDateTime nextDueDate = workflow.handleDpExpiration(debtPosition);
    WorkflowClient.start(workflow::handleDpExpiration, debtPosition);

    log.info("Start of scheduling the next debt position expiration WF: {}, on {}", debtPosition.getDebtPositionId(), nextDueDate.plusDays(1));
    String scheduledWorkflowId = generateWorkflowId(debtPosition.getDebtPositionId(), HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF);
    HandleDebtPositionExpirationWF scheduledWorkflow = workflowService.buildWorkflowStubScheduled(
      HandleDebtPositionExpirationWF.class,
      HandleDebtPositionExpirationWFImpl.TASK_QUEUE_HANDLE_DEBT_POSITION_EXPIRATION_WF,
      scheduledWorkflowId,
      Duration.between(nextDueDate.plusDays(1), OffsetDateTime.now())
    );
    WorkflowClient.start(scheduledWorkflow::handleDpExpiration, debtPosition);
    return workflowId;
  }
}
