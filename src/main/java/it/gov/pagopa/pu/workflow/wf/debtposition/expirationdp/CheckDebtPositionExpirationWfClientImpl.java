package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration.CheckDebtPositionExpirationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class CheckDebtPositionExpirationWfClientImpl implements CheckDebtPositionExpirationWfClient {

  private final WorkflowService workflowService;

  public CheckDebtPositionExpirationWfClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String checkDpExpiration(Long debtPositionId) {
    log.info("Starting check debt position expiration WF: {}", debtPositionId);
    String taskQueue = CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF;
    String workflowId = generateWorkflowId(debtPositionId, taskQueue);

    CheckDebtPositionExpirationWF workflow = workflowService.buildWorkflowStub(
      CheckDebtPositionExpirationWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::checkDpExpiration, debtPositionId);
    return workflowId;
  }

  @Override
  public void scheduleNextCheckDpExpiration(Long debtPositionId, OffsetDateTime dateTime) {
    log.info("Start of scheduling the next check debt position expiration WF: {}, on {}", debtPositionId, dateTime);
    String workflowId = generateWorkflowId(debtPositionId, CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF);
    CheckDebtPositionExpirationWF workflow = workflowService.buildWorkflowStubScheduled(
      CheckDebtPositionExpirationWF.class,
      CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF,
      workflowId,
      dateTime
    );
    WorkflowClient.start(workflow::checkDpExpiration, debtPositionId);
  }

  @Override
  public void cancelScheduling(Long debtPositionId) {
    String workflowId = generateWorkflowId(debtPositionId, CheckDebtPositionExpirationWFImpl.TASK_QUEUE_CHECK_DEBT_POSITION_EXPIRATION_WF);
    log.info("Cancelling next scheduling of workflow {}", workflowId);
    workflowService.cancelWorkflow(workflowId);
  }

}
