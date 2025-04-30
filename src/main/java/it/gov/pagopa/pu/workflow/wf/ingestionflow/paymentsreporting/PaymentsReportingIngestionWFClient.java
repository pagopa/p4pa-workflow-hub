package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.mapper.WorkflowCreatedMapper;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class PaymentsReportingIngestionWFClient {

  private final WorkflowService workflowService;

  public PaymentsReportingIngestionWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public WorkflowCreatedDTO ingest(Long ingestionFlowFileId) {
    log.info("Starting payments reporting ingestion flow file having id {}", ingestionFlowFileId);
    String taskQueue = PaymentsReportingIngestionWFImpl.TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_WF;
    String workflowId = generateWorkflowId(ingestionFlowFileId, PaymentsReportingIngestionWF.class);

    PaymentsReportingIngestionWF workflow = workflowService.buildWorkflowStub(
      PaymentsReportingIngestionWF.class,
      taskQueue,
      workflowId);
    WorkflowCreatedDTO wfExec = WorkflowCreatedMapper.map(WorkflowClient.start(workflow::ingest, ingestionFlowFileId));
    log.info("Started workflow: {}", wfExec);
    return wfExec;
  }
}
