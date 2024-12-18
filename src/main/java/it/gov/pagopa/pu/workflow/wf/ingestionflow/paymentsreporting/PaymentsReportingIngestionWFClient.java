package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.def.PaymentsReportingIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.def.PaymentsReportingIngestionWFImpl;
import org.springframework.stereotype.Service;

@Service
public class PaymentsReportingIngestionWFClient {

  private final WorkflowClient client;

  public PaymentsReportingIngestionWFClient(WorkflowClient client) {
    this.client = client;
  }

  public String ingest(Long ingestionFlowFileId){
    String workflowId = String.valueOf(ingestionFlowFileId);
    PaymentsReportingIngestionWF workflow =
      client.newWorkflowStub(
        PaymentsReportingIngestionWF.class,
        WorkflowOptions.newBuilder()
          .setTaskQueue(PaymentsReportingIngestionWFImpl.TASK_QUEUE)
          .setWorkflowId(workflowId)
          .build());
    WorkflowClient.start(workflow::ingest, ingestionFlowFileId);
    return workflowId;
  }
}
