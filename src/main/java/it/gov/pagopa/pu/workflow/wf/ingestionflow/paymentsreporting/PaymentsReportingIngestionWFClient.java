package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.def.PaymentsReportingIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.def.PaymentsReportingIngestionWFImpl;
import org.springframework.stereotype.Service;

@Service
public class PaymentsReportingIngestionWFClient {

  private final WorkflowService workflowService;

  public PaymentsReportingIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String ingest(Long ingestionFlowFileId) {
    String workflowId = String.valueOf(ingestionFlowFileId);
    PaymentsReportingIngestionWF workflow = workflowService.buildWorkflowStub(
      PaymentsReportingIngestionWF.class,
      PaymentsReportingIngestionWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::ingest, ingestionFlowFileId);
    return workflowId;
  }
}
