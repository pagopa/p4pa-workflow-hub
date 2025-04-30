package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.mapper.WorkflowCreatedMapper;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class PaymentNotificationIngestionWFClient {

  private final WorkflowService workflowService;

  public PaymentNotificationIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting payment notification ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF;
    String workflowId = generateWorkflowId(ingestionFlowFileId, PaymentNotificationIngestionWF.class);

    PaymentNotificationIngestionWF workflow = workflowService.buildWorkflowStub(
      PaymentNotificationIngestionWF.class,
      taskQueue,
      workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(WorkflowClient.start(workflow::ingest, ingestionFlowFileId));
    log.info("Started workflow: {}", wfExec);
    return wfExec;
  }
}
