package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.organization.OrganizationRetrieverService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWF;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransferClassificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;
  private final OrganizationRetrieverService organizationRetrieverService;


  public TransferClassificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService, OrganizationRetrieverService organizationRetrieverService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
    this.organizationRetrieverService = organizationRetrieverService;
  }

  public WorkflowCreatedDTO startTransferClassification(TransferClassificationStartSignalDTO signalDTO) {
    log.info("Starting Transfer Classification for semantic key: {}", signalDTO);

    Long organizationId = signalDTO.getOrgId();
    if (!organizationRetrieverService.isClassificationEnabled(organizationId)) {
      log.info("Skipping transfer Classification: organization {} has flag_classification_enabled = false", organizationId);
      return null;
    }

    String workflowId = generateWorkflowId(organizationId, signalDTO.getIuv(), signalDTO.getIur(), signalDTO.getTransferIndex());
    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(TransferClassificationWF.class, taskQueue, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      TransferClassificationWF.SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  private String generateWorkflowId(Long orgId, String iuv, String iur, int transferIndex) {
    if (orgId == null || iuv == null || iur == null) {
      throw new WorkflowInternalErrorException("[INVALID_WORKFLOW_ID] The ID or the workflow must not be null");
    }
    return Utilities.generateWorkflowId(String.format("%d-%s-%s-%d", orgId, iuv, iur, transferIndex), TransferClassificationWF.class);
  }
}
