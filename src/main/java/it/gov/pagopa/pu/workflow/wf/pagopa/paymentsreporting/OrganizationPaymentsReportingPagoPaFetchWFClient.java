package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import it.gov.pagopa.payhub.activities.connector.auth.AuthnService;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.workflow.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWF;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class OrganizationPaymentsReportingPagoPaFetchWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private final OrganizationService organizationService;
  private final BrokerService brokerService;
  private final AuthnService authnService;

  public OrganizationPaymentsReportingPagoPaFetchWFClient(
    WorkflowService workflowService,
    WorkflowClientService workflowClientService,
    OrganizationService organizationService,
    BrokerService brokerService,
    AuthnService authnService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
    this.organizationService = organizationService;
    this.brokerService = brokerService;
    this.authnService = authnService;
  }

  public WorkflowCreatedDTO retrieve(Long organizationId) {
    log.info("Starting fetch PagoPA payments reporting for the organization having id {}", organizationId);

    Organization org = organizationService.getOrganizationById(organizationId, authnService.getAccessToken());
    Boolean orgFlag = org.getFlagPaymentsReporting();

    if (Boolean.FALSE.equals(orgFlag)) {
      log.info("Skipping PagoPA payments reporting fetch: organization {} has flag_payments_reporting = false", organizationId);
      throw new ValidationException("Payments reporting disabled for organization " + organizationId);
    }

    Broker broker = brokerService.findByBrokeredOrganizationId(organizationId, authnService.getAccessToken())
      .orElseThrow(() -> new ValidationException("No broker found for organization " + organizationId));
    Long brokerId = broker.getBrokerId();
    Boolean brokerFlag = broker.getFlagPaymentsReporting();

    if (Boolean.FALSE.equals(brokerFlag)) {
      log.info("Skipping PagoPA payments reporting fetch: broker {} has flag_payments_reporting = false (organization {})", brokerId, organizationId);
      throw new ValidationException("Payments reporting disabled for broker " + brokerId);
    }

    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    String workflowId = generateWorkflowId(organizationId, PaymentsReportingPagoPaOrganizationFetchWF.class);

    PaymentsReportingPagoPaOrganizationFetchWF workflow = workflowService.buildWorkflowStubToStartNew(
      PaymentsReportingPagoPaOrganizationFetchWF.class,
      taskQueue,
      workflowId);
    return workflowClientService.start(workflow::retrieve, organizationId);
  }

  /** Cannot invoke a WF from WF thread, using Async to use an external thread instead */
  @Async
  public Future<WorkflowCreatedDTO> retrieveAsyncStart(Long organizationId) {
    return CompletableFuture.completedFuture(retrieve(organizationId));
  }

}
