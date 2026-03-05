package it.gov.pagopa.pu.workflow.wf.classification.iud;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.organization.OrganizationRetrieverService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification.IudClassificationWF;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IudClassificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private final OrganizationRetrieverService organizationRetrieverService;

  public IudClassificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService, OrganizationRetrieverService organizationRetrieverService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
    this.organizationRetrieverService = organizationRetrieverService;
  }

  public WorkflowCreatedDTO notifyReceipt(IudClassificationNotifyReceiptSignalDTO signalDTO) {
    Long organizationId = signalDTO.getOrganizationId();
    if (organizationRetrieverService.isClassificationEnabled(organizationId)) {
      log.info("Skipping IUD Classification by receipt: organization {} has flag_classification_enabled = false", organizationId);
      throw new ValidationException("Classification disabled for organization " + organizationId);
    }

    String workflowId = generateWorkflowId(organizationId, signalDTO.getIud());

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IudClassificationWF.class, taskQueue, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_RECEIPT,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  public WorkflowCreatedDTO notifyPaymentNotification(IudClassificationNotifyPaymentNotificationSignalDTO signalDTO) {
    Long organizationId = signalDTO.getOrganizationId();
    if (organizationRetrieverService.isClassificationEnabled(organizationId)) {
      log.info("Skipping IUD Classification by payments: organization {} has flag_classification_enabled = false", organizationId);
      throw new ValidationException("Classification disabled for organization " + organizationId);
    }

    String workflowId = generateWorkflowId(signalDTO.getOrganizationId(), signalDTO.getIud());

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IudClassificationWF.class, taskQueue, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENT_NOTIFICATION,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  private static String generateWorkflowId(Long organizationId, String iud) {
    if (organizationId == null || iud == null) {
      throw new WorkflowInternalErrorException("[INVALID_ORGANIZATION_ID_OR_IUD] The organizationId or iud must not be null");
    }
    return Utilities.generateWorkflowId(String.format("%d-%s", organizationId, iud), IudClassificationWF.class);
  }
}
