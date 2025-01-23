package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
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

  public String classify(Long organizationId, String iuv, String iur, int transferIndex) {
    log.info("Starting Transfer Classification for organizationId {}, iuv {}, iur {}, transferIndex {}",
      organizationId, iuv, iur, transferIndex);
    String workflowId = generateWorkflowId(organizationId, iuv, iur, transferIndex);
    TransferClassificationWF workflow = workflowService.buildWorkflowStub(
      TransferClassificationWF.class,
      TransferClassificationWFImpl.TASK_QUEUE,
      workflowId);
    WorkflowClient.start(workflow::classify, organizationId, iuv, iur, transferIndex);
    return workflowId;
  }

  private String generateWorkflowId(Long orgId, String iuv, String iur, int transferIndex) {
    if (orgId == null || iuv == null || iur == null) {
      throw new WorkflowInternalErrorException("The ID or the workflow must not be null");
    }
    return String.format("%s-%d-%s-%s-%d", TransferClassificationWFImpl.TASK_QUEUE, orgId, iuv, iur, transferIndex);
  }
}
