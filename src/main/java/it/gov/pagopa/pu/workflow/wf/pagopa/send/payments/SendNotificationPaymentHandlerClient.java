package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf.SendNotificationPaymentHandlerWF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class SendNotificationPaymentHandlerClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public SendNotificationPaymentHandlerClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO notifyPayment(DebtPositionDTO debtPosition, Long receiptId) {
    log.debug("Starting notify payment for receiptId {}", receiptId);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PAYMENT_HANDLER;
    String workflowId = generateWorkflowId(receiptId, SendNotificationPaymentHandlerWF.class);

    SendNotificationPaymentHandlerWF wf = workflowService.buildWorkflowStubToStartNew(
      SendNotificationPaymentHandlerWF.class, taskQueue, workflowId);

    return workflowClientService.start(wf::notifyPayment, debtPosition, receiptId);
  }
}
