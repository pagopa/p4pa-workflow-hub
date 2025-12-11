package it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments.wf.DeletePaidInstallmentsOnPagoPaWF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletePaidInstallmentsOnPagoPaWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public WorkflowCreatedDTO deletePaidInstallments(DebtPositionDTO  debtPositionDTO, Long receiptId) {
    log.info("Starting deletion of paid installments on PagoPA for debtPositionId={} and receiptId={}", debtPositionDTO.getDebtPositionId(), receiptId);
    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY;
    String workflowId = generateWorkflowId(receiptId, DeletePaidInstallmentsOnPagoPaWF.class);

    DeletePaidInstallmentsOnPagoPaWF workflow = workflowService.buildWorkflowStubToStartNew(
      DeletePaidInstallmentsOnPagoPaWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::deletePaidInstallments, debtPositionDTO, receiptId);
  }

}
