package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransferClassificationWFClient {

  private final WorkflowService workflowService;
  private final WorkflowClientService workflowClientService;

  public TransferClassificationWFClient(WorkflowService workflowService, WorkflowClientService workflowClientService) {
    this.workflowService = workflowService;
    this.workflowClientService = workflowClientService;
  }

  public WorkflowCreatedDTO startTransferClassification(TransferClassificationStartSignalDTO signalDTO) {
    log.info("Starting Transfer Classification for semantic key: {}", signalDTO);

    String workflowId = generateWorkflowId(signalDTO.getOrgId(), signalDTO.getIuv(), signalDTO.getIur(), signalDTO.getTransferIndex());
    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(TransferClassificationWFImpl.TASK_QUEUE_TRANSFER_CLASSIFICATION_WF, workflowId);
    return workflowClientService.signalWithStart(
      untypedWorkflowStub,
      TransferClassificationWF.SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION,
      new Object[]{signalDTO},
      new Object[]{}
    );
  }

  private String generateWorkflowId(Long orgId, String iuv, String iur, int transferIndex) {
    if (orgId == null || iuv == null || iur == null) {
      throw new WorkflowInternalErrorException("The ID or the workflow must not be null");
    }
    return Utilities.generateWorkflowId(String.format("%d-%s-%s-%d", orgId, iuv, iur, transferIndex), TransferClassificationWF.class);
  }
}
