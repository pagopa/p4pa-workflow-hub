package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class PaymentNotificationIngestionWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public PaymentNotificationIngestionWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting payment notification ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF;
    String workflowId = generateWorkflowId(ingestionFlowFileId, PaymentNotificationIngestionWF.class);

    PaymentNotificationIngestionWF workflow = workflowService.buildWorkflowStub(
      PaymentNotificationIngestionWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::ingest, ingestionFlowFileId);
  }
}
