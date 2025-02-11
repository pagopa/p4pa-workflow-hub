package it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch.PaymentsReportingPagoPaWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch.PaymentsReportingPagoPaWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class PaymentsReportingPagoPaWFClient {
  private final WorkflowService workflowService;

  public PaymentsReportingPagoPaWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String retrieve(Long organizationId) {
    log.info("Starting payments reporting ingestion flow file having id {}", organizationId);
    String workflowId = generateWorkflowId(organizationId, PaymentsReportingPagoPaWFImpl.TASK_QUEUE);
    PaymentsReportingPagoPaWF workflow = workflowService.buildWorkflowStub(
      PaymentsReportingPagoPaWF.class,
      PaymentsReportingPagoPaWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::retrieve, organizationId);
    return workflowId;
  }
}
