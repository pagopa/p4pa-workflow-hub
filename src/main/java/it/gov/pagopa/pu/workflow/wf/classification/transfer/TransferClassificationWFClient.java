package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransferClassificationWFClient {

  private final WorkflowService workflowService;

  public TransferClassificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String startTransferClassification(TransferClassificationStartSignalDTO signalDTO) {
    log.info("Starting Transfer Classification for semantic key: {}", signalDTO);

    String workflowId = generateWorkflowId(signalDTO.getOrgId(), signalDTO.getIuv(), signalDTO.getIur(), signalDTO.getTransferIndex());
    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(TransferClassificationWFImpl.TASK_QUEUE_TRANSFER_CLASSIFICATION_WF, workflowId);
    WorkflowExecution wfExecution = untypedWorkflowStub.signalWithStart(
      TransferClassificationWF.SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION,
      new Object[]{signalDTO},
      new Object[]{}
    );
    log.info("Transfer classification Workflow started with workflowId: {}", wfExecution.getWorkflowId());
    return workflowId;
  }

  private String generateWorkflowId(Long orgId, String iuv, String iur, int transferIndex) {
    if (orgId == null || iuv == null || iur == null) {
      throw new WorkflowInternalErrorException("The ID or the workflow must not be null");
    }
    return String.format("%s-%d-%s-%s-%d", TransferClassificationWFImpl.TASK_QUEUE_TRANSFER_CLASSIFICATION_WF, orgId, iuv, iur, transferIndex);
  }
}
