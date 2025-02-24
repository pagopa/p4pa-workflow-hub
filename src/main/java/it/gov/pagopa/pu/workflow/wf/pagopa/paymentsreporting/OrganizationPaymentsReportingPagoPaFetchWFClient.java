package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class OrganizationPaymentsReportingPagoPaFetchWFClient {
  private final WorkflowService workflowService;

  public OrganizationPaymentsReportingPagoPaFetchWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String retrieve(Long organizationId) {
    log.info("Starting fetch PagoPA payments reporting for the organization having id {}", organizationId);
    String taskQueue = PaymentsReportingPagoPaOrganizationFetchWFImpl.TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH;
    String workflowId = generateWorkflowId(organizationId, taskQueue);

    PaymentsReportingPagoPaOrganizationFetchWF workflow = workflowService.buildWorkflowStub(
      PaymentsReportingPagoPaOrganizationFetchWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::retrieve, organizationId);
    return workflowId;
  }
}
