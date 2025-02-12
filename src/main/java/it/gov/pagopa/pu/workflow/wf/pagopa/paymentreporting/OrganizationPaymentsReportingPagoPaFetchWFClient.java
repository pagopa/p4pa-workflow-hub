package it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch.OrganizationPaymentsReportingPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch.OrganizationPaymentsReportingPagoPaFetchWFImpl;
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
    String workflowId = generateWorkflowId(organizationId, OrganizationPaymentsReportingPagoPaFetchWFImpl.TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH);
    OrganizationPaymentsReportingPagoPaFetchWF workflow = workflowService.buildWorkflowStub(
      OrganizationPaymentsReportingPagoPaFetchWF.class,
      OrganizationPaymentsReportingPagoPaFetchWFImpl.TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH,
      workflowId);
    WorkflowClient.start(workflow::retrieve, organizationId);
    return workflowId;
  }
}
