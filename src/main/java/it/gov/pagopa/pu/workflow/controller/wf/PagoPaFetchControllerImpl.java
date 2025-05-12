package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.controller.generated.PagoPaFetchApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.OrganizationPaymentsReportingPagoPaFetchWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PagoPaFetchControllerImpl implements PagoPaFetchApi {

  private final OrganizationPaymentsReportingPagoPaFetchWFClient paymentsReportingPagoPaFetchWFClient;

  public PagoPaFetchControllerImpl(OrganizationPaymentsReportingPagoPaFetchWFClient paymentsReportingPagoPaFetchWFClient) {
    this.paymentsReportingPagoPaFetchWFClient = paymentsReportingPagoPaFetchWFClient;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> fetchOrganizationPaymentsReporting(Long organizationId) {
    log.info("Requesting to fetch payments reporting data from PagoPa related to organization {}", organizationId);
    return ResponseEntity.ok(paymentsReportingPagoPaFetchWFClient.retrieve(organizationId));
  }
}
