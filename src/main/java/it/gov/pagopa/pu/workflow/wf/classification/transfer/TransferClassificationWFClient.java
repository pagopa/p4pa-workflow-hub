package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWFImpl;
import org.springframework.stereotype.Service;

@Service
public class TransferClassificationWFClient {

  private final WorkflowService workflowService;

  public TransferClassificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String classify(Long orgId, String iuv, String iur, int transferIndex) {
    String workflowId = generateWorkflowId(orgId, iuv, iur, transferIndex, TransferClassificationWFImpl.TASK_QUEUE);
    TransferClassificationWF workflow = workflowService.buildWorkflowStub(
      TransferClassificationWF.class,
      TransferClassificationWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::classify, orgId, iuv, iur, transferIndex);
    return workflowId;
  }

  private String generateWorkflowId(Long orgId, String iuv, String iur, int transferIndex, String workflow) {
    if (orgId == null || iuv == null || iur == null || workflow == null) {
      throw new WorkflowInternalErrorException("The ID or the workflow must not be null");
    }
    return String.format("%s-%d-%s-%s-%d", workflow, orgId, iuv, iur, transferIndex);
  }
}
