package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine.SynchronizeFineWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfsynchronizefine.SynchronizeFineWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class DebtPositionFineClientImpl implements DebtPositionFineClient {

  private final WorkflowService workflowService;

  public DebtPositionFineClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String expireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig) {
    log.info("Starting check debt position reduction expiration WF: {}", debtPositionId);
    String taskQueue = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION;
    String workflowId = generateExpireFineReductionWorkflowId(debtPositionId);

    FineReductionOptionExpirationWF workflow = workflowService.buildWorkflowStub(
      FineReductionOptionExpirationWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::expireFineReduction, debtPositionId, wfExecutionConfig);
    return workflowId;
  }

  @Override
  public String synchronizeFine(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, Boolean massive, FineWfExecutionConfig wfExecutionConfig) {
    log.info("Starting synchronizing fine WF: {}", debtPositionDTO.getDebtPositionId());
    String taskQueue = SynchronizeFineWFImpl.TASK_QUEUE_SYNC_FINE;
    String workflowId = generateWorkflowId(debtPositionDTO.getDebtPositionId(), SynchronizeFineWF.class);

    SynchronizeFineWF workflow = workflowService.buildWorkflowStub(
      SynchronizeFineWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::synchronizeFine, debtPositionDTO, paymentEventRequest, massive, wfExecutionConfig);
    return workflowId;
  }

  public static String generateExpireFineReductionWorkflowId(Long debtPositionId) {
    return generateWorkflowId(debtPositionId, FineReductionOptionExpirationWF.class);
  }
}
