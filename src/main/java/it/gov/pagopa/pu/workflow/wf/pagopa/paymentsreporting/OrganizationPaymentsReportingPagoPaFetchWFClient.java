package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWF;
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

  public OrganizationPaymentsReportingPagoPaFetchWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO retrieve(Long organizationId) {
    log.info("Starting fetch PagoPA payments reporting for the organization having id {}", organizationId);
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
