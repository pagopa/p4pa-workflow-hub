package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Service
public class PaymentsReportingIngestionWFClient {

  private final WorkflowService workflowService;

  public PaymentsReportingIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String ingest(Long ingestionFlowFileId) {
    String workflowId = generateWorkflowId(ingestionFlowFileId, PaymentsReportingIngestionWFImpl.TASK_QUEUE);
    PaymentsReportingIngestionWF workflow = workflowService.buildWorkflowStub(
      PaymentsReportingIngestionWF.class,
      PaymentsReportingIngestionWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::ingest, ingestionFlowFileId);
    return workflowId;
  }
}
