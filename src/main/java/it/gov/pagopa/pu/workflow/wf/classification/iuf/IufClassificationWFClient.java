package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.classification.IufReceiptClassificationWFImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufReceiptClassificationForReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufReceiptClassificationForTreasurySignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IufClassificationWFClient {

  private final WorkflowService workflowService;

  public IufClassificationWFClient(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  public String notifyTreasury(Long organizationId, String treasuryId, String iuf) {

    IufReceiptClassificationForTreasurySignalDTO signalDTO = IufReceiptClassificationForTreasurySignalDTO.builder()
      .organizationId(organizationId)
      .iuf(iuf)
      .treasuryId(treasuryId)
      .build();

    String workflowId = generateWorkflowId(IufReceiptClassificationWFImpl.TASK_QUEUE, organizationId, iuf);

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufReceiptClassificationWFImpl.TASK_QUEUE, workflowId);
    WorkflowExecution wfExecution = untypedWorkflowStub.signalWithStart(
      IufReceiptClassificationForTreasurySignalDTO.SIGNAL_METHOD_NAME,
      new Object[]{signalDTO},
      new Object[]{}
    );

    log.info("IUF receipt classification Workflow for Treasury started with workflowId: {}", wfExecution.getWorkflowId());
    return workflowId;

  }

  public String notifyPaymentsReporting(Long organizationId, String iuf, List<Transfer2ClassifyDTO> transfers2classify) {

    IufReceiptClassificationForReportingSignalDTO signalDTO = IufReceiptClassificationForReportingSignalDTO.builder()
      .organizationId(organizationId)
      .iuf(iuf)
      .transfers2classify(transfers2classify)
      .build();

    String workflowId = generateWorkflowId(IufReceiptClassificationWFImpl.TASK_QUEUE, organizationId, iuf);

    WorkflowStub untypedWorkflowStub = workflowService.buildUntypedWorkflowStub(IufReceiptClassificationWFImpl.TASK_QUEUE, workflowId);
    WorkflowExecution wfExecution = untypedWorkflowStub.signalWithStart(
      IufReceiptClassificationForReportingSignalDTO.SIGNAL_METHOD_NAME,
      new Object[]{signalDTO},
      new Object[]{}
    );

    log.info("IUF receipt classification Workflow for reporting started with workflowId: {}", wfExecution.getWorkflowId());
    return workflowId;

  }

 private static String generateWorkflowId(String workflow, Long organizationId, String iuf) {
  if (workflow == null || organizationId == null || iuf == null) {
    throw new WorkflowInternalErrorException("The workflow or organizationId or iuf must not be null");
  }
  return String.format("%s-%d-%s", workflow, organizationId, iuf);
}

}
